package com.oppo.statistics.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzipUtil {
    private static final int IO_BUF_SIZE = 1024;

    public static byte[] compress(String str) {
        if (str == null || str.length() == 0) {
            return "".getBytes();
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(str.getBytes());
        GZIPOutputStream gzipOut = null;
        try {
            GZIPOutputStream gzipOut2 = new GZIPOutputStream(out);
            byte[] buf = new byte[IO_BUF_SIZE];
            while (true) {
                int size = in.read(buf);
                if (size > 0) {
                    gzipOut2.write(buf, 0, size);
                    gzipOut2.flush();
                } else {
                    try {
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            in.close();
            gzipOut2.close();
            out.close();
        } catch (IOException e2) {
            LogUtil.e(e2);
            in.close();
            gzipOut.close();
            out.close();
        } catch (Throwable th) {
            try {
                in.close();
                gzipOut.close();
                out.close();
            } catch (IOException e3) {
                e3.printStackTrace();
            }
            throw th;
        }
        return out.toByteArray();
    }

    public static String uncompress(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        GZIPInputStream gzip = null;
        try {
            GZIPInputStream gzip2 = new GZIPInputStream(in);
            byte[] buf = new byte[IO_BUF_SIZE];
            while (true) {
                int size = gzip2.read(buf);
                if (size > 0) {
                    out.write(buf, 0, size);
                    out.flush();
                } else {
                    try {
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            in.close();
            gzip2.close();
            out.close();
        } catch (IOException e2) {
            LogUtil.e(e2);
            in.close();
            gzip.close();
            out.close();
        } catch (Throwable th) {
            try {
                in.close();
                gzip.close();
                out.close();
            } catch (IOException e3) {
                e3.printStackTrace();
            }
            throw th;
        }
        return out.toString();
    }
}
