package com.android.internal.telephony.oem.rus;

import android.hardware.radio.V1_0.RadioAccessFamily;
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

    /* JADX WARNING: Removed duplicated region for block: B:15:0x003b A:{SYNTHETIC, Splitter: B:15:0x003b} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0044 A:{SYNTHETIC, Splitter: B:20:0x0044} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public File saveToFile(String content, String destfile) {
        Exception e;
        Throwable th;
        File file = new File(destfile);
        FileOutputStream outStream = null;
        try {
            FileOutputStream outStream2 = new FileOutputStream(file);
            try {
                outStream2.write(content.getBytes());
                if (outStream2 != null) {
                    try {
                        outStream2.close();
                    } catch (Exception e2) {
                    }
                }
                outStream = outStream2;
            } catch (Exception e3) {
                e = e3;
                outStream = outStream2;
                try {
                    Log.d("wxk", "this is some wrong =" + e);
                    e.printStackTrace();
                    if (outStream != null) {
                    }
                    return file;
                } catch (Throwable th2) {
                    th = th2;
                    if (outStream != null) {
                        try {
                            outStream.close();
                        } catch (Exception e4) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                outStream = outStream2;
                if (outStream != null) {
                }
                throw th;
            }
        } catch (Exception e5) {
            e = e5;
            Log.d("wxk", "this is some wrong =" + e);
            e.printStackTrace();
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (Exception e6) {
                }
            }
            return file;
        }
        return file;
    }

    public void saveSpnToFile(String content, String destFile) {
        Exception e;
        Throwable th;
        FileOutputStream outStream = null;
        try {
            FileOutputStream outStream2 = new FileOutputStream(new File(destFile));
            try {
                outStream2.write(content.getBytes());
                try {
                    outStream2.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                outStream = outStream2;
            } catch (Exception e3) {
                e2 = e3;
                outStream = outStream2;
                try {
                    Log.d("wxk", "this is some wrong =" + e2);
                    e2.printStackTrace();
                    try {
                        outStream.close();
                    } catch (Exception e22) {
                        e22.printStackTrace();
                    }
                } catch (Throwable th2) {
                    th = th2;
                    try {
                        outStream.close();
                    } catch (Exception e222) {
                        e222.printStackTrace();
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                outStream = outStream2;
                outStream.close();
                throw th;
            }
        } catch (Exception e4) {
            e222 = e4;
            Log.d("wxk", "this is some wrong =" + e222);
            e222.printStackTrace();
            outStream.close();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x0059 A:{SYNTHETIC, Splitter: B:31:0x0059} */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x003e A:{SYNTHETIC, Splitter: B:16:0x003e} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public File write2SDFromInput(String path, String fileName, InputStream input) {
        Exception e;
        Throwable th;
        File file = null;
        OutputStream output = null;
        try {
            creatSDDir(path);
            file = creatSDFile(path + fileName);
            OutputStream output2 = new FileOutputStream(file);
            try {
                byte[] buffer = new byte[RadioAccessFamily.EVDO_B];
                if (output2 != null) {
                    while (true) {
                        int currentRead = input.read(buffer, 0, 4095);
                        if (currentRead <= 0) {
                            break;
                        }
                        output2.write(buffer, 0, currentRead);
                    }
                    output2.flush();
                }
                if (output2 != null) {
                    try {
                        output2.close();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            } catch (Exception e3) {
                e2 = e3;
                output = output2;
                try {
                    e2.printStackTrace();
                    if (output != null) {
                        try {
                            output.close();
                        } catch (Exception e22) {
                            e22.printStackTrace();
                        }
                    }
                    return file;
                } catch (Throwable th2) {
                    th = th2;
                    if (output != null) {
                        try {
                            output.close();
                        } catch (Exception e222) {
                            e222.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                output = output2;
                if (output != null) {
                }
                throw th;
            }
        } catch (Exception e4) {
            e222 = e4;
            e222.printStackTrace();
            if (output != null) {
            }
            return file;
        }
        return file;
    }
}
