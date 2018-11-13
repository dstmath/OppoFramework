package libcore.tzdata.update;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class ConfigBundle {
    private static final int BUFFER_SIZE = 8192;
    public static final String CHECKSUMS_FILE_NAME = "checksums";
    public static final String ICU_DATA_FILE_NAME = "icu/icu_tzdata.dat";
    public static final String TZ_DATA_VERSION_FILE_NAME = "tzdata_version";
    public static final String ZONEINFO_FILE_NAME = "tzdata";
    private final byte[] bytes;

    public ConfigBundle(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getBundleBytes() {
        return this.bytes;
    }

    public void extractTo(File targetDir) throws IOException {
        extractZipSafely(new ByteArrayInputStream(this.bytes), targetDir, true);
    }

    /* JADX WARNING: Removed duplicated region for block: B:47:0x006e A:{Splitter: B:4:0x000c, ExcHandler: all (th java.lang.Throwable)} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x006e A:{Splitter: B:4:0x000c, ExcHandler: all (th java.lang.Throwable)} */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing block: B:47:0x006e, code:
            r10 = th;
     */
    /* JADX WARNING: Missing block: B:48:0x006f, code:
            r11 = null;
            r8 = r9;
     */
    /* JADX WARNING: Missing block: B:57:0x0081, code:
            r11 = th;
     */
    /* JADX WARNING: Missing block: B:58:0x0083, code:
            r13 = move-exception;
     */
    /* JADX WARNING: Missing block: B:59:0x0084, code:
            if (r11 == null) goto L_0x0086;
     */
    /* JADX WARNING: Missing block: B:60:0x0086, code:
            r11 = r13;
     */
    /* JADX WARNING: Missing block: B:61:0x0088, code:
            if (r11 != r13) goto L_0x008a;
     */
    /* JADX WARNING: Missing block: B:62:0x008a, code:
            r11.addSuppressed(r13);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static void extractZipSafely(InputStream is, File targetDir, boolean makeWorldReadable) throws IOException {
        Throwable th;
        FileUtils.ensureDirectoriesExist(targetDir, makeWorldReadable);
        Throwable th2 = null;
        ZipInputStream zipInputStream = null;
        Throwable th3;
        Throwable th4;
        try {
            ZipInputStream zipInputStream2 = new ZipInputStream(is);
            try {
                FileOutputStream fos;
                byte[] buffer = new byte[8192];
                while (true) {
                    ZipEntry entry = zipInputStream2.getNextEntry();
                    if (entry != null) {
                        File entryFile = FileUtils.createSubFile(targetDir, entry.getName());
                        if (entry.isDirectory()) {
                            FileUtils.ensureDirectoriesExist(entryFile, makeWorldReadable);
                        } else {
                            if (!entryFile.getParentFile().exists()) {
                                FileUtils.ensureDirectoriesExist(entryFile.getParentFile(), makeWorldReadable);
                            }
                            th3 = null;
                            fos = null;
                            try {
                                FileOutputStream fos2 = new FileOutputStream(entryFile);
                                while (true) {
                                    try {
                                        int count = zipInputStream2.read(buffer);
                                        if (count == -1) {
                                            break;
                                        }
                                        fos2.write(buffer, 0, count);
                                    } catch (Throwable th5) {
                                        th4 = th5;
                                        fos = fos2;
                                    }
                                }
                                fos2.getFD().sync();
                                if (fos2 != null) {
                                    fos2.close();
                                }
                                if (th3 != null) {
                                    throw th3;
                                } else if (makeWorldReadable) {
                                    FileUtils.makeWorldReadable(entryFile);
                                }
                            } catch (Throwable th6) {
                                th4 = th6;
                            }
                        }
                    } else {
                        if (zipInputStream2 != null) {
                            try {
                                zipInputStream2.close();
                            } catch (Throwable th7) {
                                th2 = th7;
                            }
                        }
                        if (th2 != null) {
                            throw th2;
                        }
                        return;
                    }
                }
                try {
                    throw th4;
                } catch (Throwable th32) {
                    th = th32;
                    th32 = th4;
                    th4 = th;
                }
                if (fos != null) {
                    fos.close();
                }
                if (th32 != null) {
                    throw th32;
                } else {
                    throw th4;
                }
            } catch (Throwable th8) {
            }
        } catch (Throwable th9) {
            th4 = th9;
            th32 = null;
            if (zipInputStream != null) {
                try {
                    zipInputStream.close();
                } catch (Throwable th22) {
                    if (th32 == null) {
                        th32 = th22;
                    } else if (th32 != th22) {
                        th32.addSuppressed(th22);
                    }
                }
            }
            if (th32 != null) {
                throw th32;
            }
            throw th4;
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return Arrays.equals(this.bytes, ((ConfigBundle) o).bytes);
    }
}
