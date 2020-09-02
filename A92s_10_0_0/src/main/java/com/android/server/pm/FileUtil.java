package com.android.server.pm;

import android.text.TextUtils;
import android.util.Slog;
import com.android.server.policy.OppoPhoneWindowManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileUtil {
    private static final int CHECK_MASK = 255;
    private static final int CHECK_NUM = 1;
    private static final String TAG = "FileUtil";
    private static FileUtil sFileUtil = new FileUtil();

    private FileUtil() {
    }

    public static synchronized FileUtil getInstance() {
        FileUtil fileUtil;
        synchronized (FileUtil.class) {
            fileUtil = sFileUtil;
        }
        return fileUtil;
    }

    public String getFileMd5(File f) {
        if (f == null || !f.exists()) {
            Slog.e(TAG, "getFileMd5 can't find file");
            return null;
        }
        InputStream is = null;
        byte[] buffer = new byte[OppoPhoneWindowManager.SPEECH_START_TYPE_VALUE];
        try {
            MessageDigest md5 = MessageDigest.getInstance("md5");
            try {
                InputStream is2 = new FileInputStream(f);
                while (true) {
                    int readed = is2.read(buffer);
                    if (readed <= 0) {
                        break;
                    }
                    byte[] tmp = new byte[readed];
                    System.arraycopy(buffer, 0, tmp, 0, readed);
                    md5.update(tmp);
                }
                String bytes2hex = bytes2hex(md5.digest(), true);
                try {
                    is2.close();
                } catch (IOException e) {
                }
                return bytes2hex;
            } catch (FileNotFoundException e2) {
                Slog.i(TAG, "" + e2);
                if (is != null) {
                    is.close();
                }
            } catch (IOException e3) {
                Slog.i(TAG, "" + e3);
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e4) {
                    }
                }
            } catch (Throwable th) {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e5) {
                    }
                }
                throw th;
            }
        } catch (NoSuchAlgorithmException e6) {
            return null;
        }
        return null;
    }

    public String bytes2hex(byte[] b, boolean isLowerCase) {
        if (b == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (byte b2 : b) {
            String stmp = Integer.toHexString(b2 & 255);
            if (stmp.length() == 1) {
                builder.append("0");
                builder.append(stmp);
            } else {
                builder.append(stmp);
            }
        }
        String sb = builder.toString();
        return isLowerCase ? sb.toLowerCase() : sb.toUpperCase();
    }

    public byte[] hex2bytes(String input) {
        if (TextUtils.isEmpty(input) || input.length() % 2 != 0) {
            return null;
        }
        byte[] bytes = new byte[(input.length() / 2)];
        int i = 0;
        while (i < bytes.length) {
            try {
                bytes[i] = (byte) Integer.parseInt(input.substring(i * 2, (i * 2) + 2), 16);
                i++;
            } catch (NumberFormatException ex) {
                Slog.e(TAG, "hex2bytes fail: " + ex.getMessage());
                return null;
            }
        }
        return bytes;
    }

    public String computeDigest(byte[] input, String algorithm, boolean isLowerCase) {
        if (input == null || input.length == 0 || TextUtils.isEmpty(algorithm)) {
            return null;
        }
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
            if (messageDigest == null) {
                return null;
            }
            messageDigest.update(input);
            return bytes2hex(messageDigest.digest(), isLowerCase);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
}
