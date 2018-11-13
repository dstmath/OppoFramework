package com.oppo.statistics.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzipUtil {
    private static final int IO_BUF_SIZE = 1024;

    public static byte[] compress(String str) {
        Exception e;
        Throwable th;
        if (str == null || str.length() == 0) {
            return "".getBytes();
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(str.getBytes());
        GZIPOutputStream gzipOut = null;
        try {
            GZIPOutputStream gzipOut2 = new GZIPOutputStream(out);
            try {
                byte[] buf = new byte[IO_BUF_SIZE];
                while (true) {
                    int size = in.read(buf);
                    if (size > 0) {
                        gzipOut2.write(buf, 0, size);
                        gzipOut2.flush();
                    } else {
                        try {
                            break;
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                }
                in.close();
                gzipOut2.close();
                out.close();
            } catch (IOException e3) {
                e = e3;
                gzipOut = gzipOut2;
                try {
                    LogUtil.e(e);
                    try {
                        in.close();
                        gzipOut.close();
                        out.close();
                    } catch (IOException e22) {
                        e22.printStackTrace();
                    }
                    return out.toByteArray();
                } catch (Throwable th2) {
                    th = th2;
                    try {
                        in.close();
                        gzipOut.close();
                        out.close();
                    } catch (IOException e222) {
                        e222.printStackTrace();
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                gzipOut = gzipOut2;
                in.close();
                gzipOut.close();
                out.close();
                throw th;
            }
        } catch (IOException e4) {
            e = e4;
            LogUtil.e(e);
            in.close();
            gzipOut.close();
            out.close();
            return out.toByteArray();
        }
        return out.toByteArray();
    }

    public static String uncompress(byte[] bytes) {
        Exception e;
        Throwable th;
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        String result = "";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        GZIPInputStream gzip = null;
        try {
            GZIPInputStream gzip2 = new GZIPInputStream(in);
            try {
                byte[] buf = new byte[IO_BUF_SIZE];
                while (true) {
                    int size = gzip2.read(buf);
                    if (size > 0) {
                        out.write(buf, 0, size);
                        out.flush();
                    } else {
                        try {
                            break;
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                }
                in.close();
                gzip2.close();
                out.close();
            } catch (IOException e3) {
                e = e3;
                gzip = gzip2;
                try {
                    LogUtil.e(e);
                    try {
                        in.close();
                        gzip.close();
                        out.close();
                    } catch (IOException e22) {
                        e22.printStackTrace();
                    }
                    return out.toString();
                } catch (Throwable th2) {
                    th = th2;
                    try {
                        in.close();
                        gzip.close();
                        out.close();
                    } catch (IOException e222) {
                        e222.printStackTrace();
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                gzip = gzip2;
                in.close();
                gzip.close();
                out.close();
                throw th;
            }
        } catch (IOException e4) {
            e = e4;
            LogUtil.e(e);
            in.close();
            gzip.close();
            out.close();
            return out.toString();
        }
        return out.toString();
    }
}
