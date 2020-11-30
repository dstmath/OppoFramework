package com.oppo.internal.telephony.rus;

import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {
    private String mSDPath = (Environment.getExternalStorageDirectory() + "/");

    public String getSDPath() {
        return this.mSDPath;
    }

    public File creatSDFile(String fileName) throws IOException {
        File file = new File(this.mSDPath + fileName);
        Log.d("wys", "mSDPath = " + this.mSDPath + ",fileName = " + fileName);
        file.createNewFile();
        return file;
    }

    public File creatSDDir(String dirName) {
        File dir = new File(this.mSDPath + dirName);
        if (!dir.exists()) {
            dir.mkdir();
        }
        return dir;
    }

    public boolean isFileExist(String fileName) {
        return new File(this.mSDPath + fileName).exists();
    }

    public void deleteExistFile(String fileName) {
        File file = new File(this.mSDPath + fileName);
        if (file.exists()) {
            file.delete();
        }
    }

    public File saveToFile(String content, String destfile) {
        File file = new File(destfile);
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(file);
            outStream.write(content.getBytes());
            try {
                outStream.close();
            } catch (Exception e) {
            }
        } catch (Exception e2) {
            Log.d("wxk", "this is some wrong =" + e2);
            e2.printStackTrace();
            if (outStream != null) {
                outStream.close();
            }
        } catch (Throwable th) {
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (Exception e3) {
                }
            }
            throw th;
        }
        return file;
    }

    public void saveSpnToFile(String content, String destFile) {
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(new File(destFile));
            outStream.write(content.getBytes());
            try {
                outStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e2) {
            Log.d("wxk", "this is some wrong =" + e2);
            e2.printStackTrace();
            outStream.close();
        } catch (Throwable th) {
            try {
                outStream.close();
            } catch (Exception e3) {
                e3.printStackTrace();
            }
            throw th;
        }
    }

    public File write2SDFromInput(String path, String fileName, InputStream input) {
        File file = null;
        OutputStream output = null;
        try {
            creatSDDir(path);
            file = creatSDFile(path + fileName);
            OutputStream output2 = new FileOutputStream(file);
            byte[] buffer = new byte[(4 * 1024)];
            while (true) {
                int currentRead = input.read(buffer, 0, (4 * 1024) - 1);
                if (currentRead <= 0) {
                    break;
                }
                output2.write(buffer, 0, currentRead);
            }
            output2.flush();
            try {
                output2.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e2) {
            e2.printStackTrace();
            if (0 != 0) {
                output.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    output.close();
                } catch (Exception e3) {
                    e3.printStackTrace();
                }
            }
            throw th;
        }
        return file;
    }
}
