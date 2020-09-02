package com.mediatek.omadm;

import android.os.ParcelFileDescriptor;
import android.util.Slog;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {
    private static final String[] FILE_PATHS = {"/data/vendor/verizon/dmclient/data/updateInfo.json", "/data/vendor/verizon/dmclient/data/last_update_firmware_version"};
    /* access modifiers changed from: private */
    public static final String TAG = (OmadmServiceImpl.class.getSimpleName() + "." + FileUtils.class.getSimpleName());

    public static boolean checkPathAllow(String path) {
        for (String allowedPath : FILE_PATHS) {
            if (allowedPath.equals(path)) {
                return true;
            }
        }
        return false;
    }

    public static ParcelFileDescriptor pipeTo(InputStream inputStream) throws IOException {
        ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();
        ParcelFileDescriptor readSide = pipe[0];
        new TransferThread(new ParcelFileDescriptor.AutoCloseOutputStream(pipe[1]), inputStream).start();
        return readSide;
    }

    static class TransferThread extends Thread {
        private static final int BUFF_SIZE = 1024;
        final InputStream mIn;
        final OutputStream mOut;

        TransferThread(OutputStream out, InputStream in) {
            super("ParcelFileDescriptor Transfer Thread");
            this.mIn = in;
            this.mOut = out;
            setDaemon(true);
        }

        public void run() {
            byte[] buf = new byte[BUFF_SIZE];
            while (true) {
                try {
                    int len = this.mIn.read(buf);
                    if (len <= 0) {
                        break;
                    }
                    this.mOut.write(buf, 0, len);
                } catch (IOException e) {
                    Slog.e(FileUtils.TAG, "TransferThread", e);
                    try {
                        this.mIn.close();
                    } catch (IOException e2) {
                    }
                    try {
                        this.mOut.close();
                        return;
                    } catch (IOException e3) {
                        return;
                    }
                } catch (Throwable th) {
                    try {
                        this.mIn.close();
                    } catch (IOException e4) {
                    }
                    try {
                        this.mOut.close();
                    } catch (IOException e5) {
                    }
                    throw th;
                }
            }
            this.mOut.flush();
            try {
                this.mIn.close();
            } catch (IOException e6) {
            }
            try {
                this.mOut.close();
            } catch (IOException e7) {
            }
        }
    }
}
