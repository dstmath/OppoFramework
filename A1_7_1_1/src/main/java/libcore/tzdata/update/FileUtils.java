package libcore.tzdata.update;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.CRC32;

public final class FileUtils {
    private FileUtils() {
    }

    public static File createSubFile(File parentDir, String name) throws IOException {
        File subFile = new File(parentDir, name).getCanonicalFile();
        if (subFile.getPath().startsWith(parentDir.getCanonicalPath())) {
            return subFile;
        }
        throw new IOException(name + " must exist beneath " + parentDir + ". Canonicalized subpath: " + subFile);
    }

    public static void ensureDirectoriesExist(File dir, boolean makeWorldReadable) throws IOException {
        LinkedList<File> dirs = new LinkedList();
        File currentDir = dir;
        do {
            dirs.addFirst(currentDir);
            currentDir = currentDir.getParentFile();
        } while (currentDir != null);
        for (File dirToCheck : dirs) {
            if (dirToCheck.exists()) {
                if (!dirToCheck.isDirectory()) {
                    throw new IOException(dirToCheck + " exists but is not a directory");
                }
            } else if (!dirToCheck.mkdir()) {
                throw new IOException("Unable to create directory: " + dir);
            } else if (makeWorldReadable) {
                makeDirectoryWorldAccessible(dirToCheck);
            }
        }
    }

    public static void makeDirectoryWorldAccessible(File directory) throws IOException {
        if (directory.isDirectory()) {
            makeWorldReadable(directory);
            if (!directory.setExecutable(true, false)) {
                throw new IOException("Unable to make " + directory + " world-executable");
            }
            return;
        }
        throw new IOException(directory + " must be a directory");
    }

    public static void makeWorldReadable(File file) throws IOException {
        if (!file.setReadable(true, false)) {
            throw new IOException("Unable to make " + file + " world-readable");
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0027 A:{SYNTHETIC, Splitter: B:17:0x0027} */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0042  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x002c  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static long calculateChecksum(File file) throws IOException {
        Throwable th;
        Throwable th2 = null;
        CRC32 crc32 = new CRC32();
        FileInputStream fis = null;
        try {
            FileInputStream fis2 = new FileInputStream(file);
            try {
                byte[] buffer = new byte[8196];
                while (true) {
                    int count = fis2.read(buffer);
                    if (count == -1) {
                        break;
                    }
                    crc32.update(buffer, 0, count);
                }
                if (fis2 != null) {
                    try {
                        fis2.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 == null) {
                    return crc32.getValue();
                }
                throw th2;
            } catch (Throwable th4) {
                th = th4;
                fis = fis2;
                if (fis != null) {
                }
                if (th2 == null) {
                }
            }
        } catch (Throwable th5) {
            th = th5;
            if (fis != null) {
                try {
                    fis.close();
                } catch (Throwable th6) {
                    if (th2 == null) {
                        th2 = th6;
                    } else if (th2 != th6) {
                        th2.addSuppressed(th6);
                    }
                }
            }
            if (th2 == null) {
                throw th2;
            }
            throw th;
        }
    }

    public static void rename(File from, File to) throws IOException {
        ensureFileDoesNotExist(to);
        if (!from.renameTo(to)) {
            throw new IOException("Unable to rename " + from + " to " + to);
        }
    }

    public static void ensureFileDoesNotExist(File file) throws IOException {
        if (!file.exists()) {
            return;
        }
        if (file.isFile()) {
            doDelete(file);
            return;
        }
        throw new IOException(file + " is not a file");
    }

    public static void doDelete(File file) throws IOException {
        if (!file.delete()) {
            throw new IOException("Unable to delete: " + file);
        }
    }

    public static boolean isSymlink(File file) throws IOException {
        return !file.getCanonicalPath().equals(new File(file.getParentFile().getCanonicalFile(), file.getName()).getPath());
    }

    public static void deleteRecursive(File toDelete) throws IOException {
        if (toDelete.isDirectory()) {
            for (File file : toDelete.listFiles()) {
                if (!file.isDirectory() || isSymlink(file)) {
                    doDelete(file);
                } else {
                    deleteRecursive(file);
                }
            }
            String[] remainingFiles = toDelete.list();
            if (remainingFiles.length != 0) {
                throw new IOException("Unable to delete files: " + Arrays.toString(remainingFiles));
            }
        }
        doDelete(toDelete);
    }

    public static boolean filesExist(File rootDir, String... fileNames) throws IOException {
        for (String fileName : fileNames) {
            if (!new File(rootDir, fileName).exists()) {
                return false;
            }
        }
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x002b A:{SYNTHETIC, Splitter: B:16:0x002b} */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0047  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0030  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static List<String> readLines(File file) throws IOException {
        Throwable th;
        Throwable th2 = null;
        BufferedReader fileReader = null;
        try {
            BufferedReader fileReader2 = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            try {
                List<String> lines = new ArrayList();
                while (true) {
                    String line = fileReader2.readLine();
                    if (line == null) {
                        break;
                    }
                    lines.add(line);
                }
                if (fileReader2 != null) {
                    try {
                        fileReader2.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 == null) {
                    return lines;
                }
                throw th2;
            } catch (Throwable th4) {
                th = th4;
                fileReader = fileReader2;
                if (fileReader != null) {
                }
                if (th2 == null) {
                }
            }
        } catch (Throwable th5) {
            th = th5;
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (Throwable th6) {
                    if (th2 == null) {
                        th2 = th6;
                    } else if (th2 != th6) {
                        th2.addSuppressed(th6);
                    }
                }
            }
            if (th2 == null) {
                throw th2;
            }
            throw th;
        }
    }
}
