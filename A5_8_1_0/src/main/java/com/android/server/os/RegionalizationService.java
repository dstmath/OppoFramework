package com.android.server.os;

import android.text.TextUtils;
import android.util.Log;
import com.android.internal.os.IRegionalizationService.Stub;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class RegionalizationService extends Stub {
    private static final String TAG = "RegionalizationService";

    public boolean checkFileExists(String filepath) {
        File file = new File(filepath);
        if (file == null || (file.exists() ^ 1) != 0) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x006a A:{SYNTHETIC, Splitter: B:29:0x006a} */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x006f A:{Catch:{ IOException -> 0x00b2 }} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x007c A:{SYNTHETIC, Splitter: B:40:0x007c} */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x0081 A:{Catch:{ IOException -> 0x00d2 }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public ArrayList<String> readFile(String filepath, String regularExpression) {
        IOException e;
        Throwable th;
        File file = new File(filepath);
        if (file == null || (file.exists() ^ 1) != 0 || (file.canRead() ^ 1) != 0) {
            return null;
        }
        ArrayList<String> contents = new ArrayList();
        FileReader fr = null;
        BufferedReader br = null;
        try {
            FileReader fr2 = new FileReader(file);
            try {
                BufferedReader br2 = new BufferedReader(fr2);
                while (true) {
                    try {
                        String line = br2.readLine();
                        if (line == null) {
                            break;
                        }
                        line = line.trim();
                        if (line == null) {
                            break;
                        } else if (TextUtils.isEmpty(regularExpression)) {
                            contents.add(line);
                        } else if (line.matches(regularExpression)) {
                            contents.add(line);
                        }
                    } catch (IOException e2) {
                        e = e2;
                        br = br2;
                        fr = fr2;
                    } catch (Throwable th2) {
                        th = th2;
                        br = br2;
                        fr = fr2;
                    }
                }
                if (br2 != null) {
                    try {
                        br2.close();
                    } catch (IOException e3) {
                        Log.e(TAG, "Close the reader error, caused by: " + e3.getMessage());
                    }
                }
                if (fr2 != null) {
                    fr2.close();
                }
            } catch (IOException e4) {
                e3 = e4;
                fr = fr2;
                try {
                    Log.e(TAG, "Read File error, caused by: " + e3.getMessage());
                    if (br != null) {
                    }
                    if (fr != null) {
                    }
                    return contents;
                } catch (Throwable th3) {
                    th = th3;
                    if (br != null) {
                        try {
                            br.close();
                        } catch (IOException e32) {
                            Log.e(TAG, "Close the reader error, caused by: " + e32.getMessage());
                            throw th;
                        }
                    }
                    if (fr != null) {
                        fr.close();
                    }
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
                fr = fr2;
                if (br != null) {
                }
                if (fr != null) {
                }
                throw th;
            }
        } catch (IOException e5) {
            e32 = e5;
            Log.e(TAG, "Read File error, caused by: " + e32.getMessage());
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e322) {
                    Log.e(TAG, "Close the reader error, caused by: " + e322.getMessage());
                }
            }
            if (fr != null) {
                fr.close();
            }
            return contents;
        }
        return contents;
    }

    /* JADX WARNING: Removed duplicated region for block: B:27:0x003b A:{SYNTHETIC, Splitter: B:27:0x003b} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0047 A:{SYNTHETIC, Splitter: B:35:0x0047} */
    /* JADX WARNING: Missing block: B:6:0x0018, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean writeFile(String filepath, String content, boolean append) {
        IOException e;
        Throwable th;
        File file = new File(filepath);
        if (file == null || (file.exists() ^ 1) != 0 || (file.canWrite() ^ 1) != 0 || TextUtils.isEmpty(content)) {
            return false;
        }
        FileWriter fw = null;
        try {
            FileWriter fw2 = new FileWriter(file, append);
            try {
                fw2.write(content);
                if (fw2 != null) {
                    try {
                        fw2.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                        return false;
                    }
                }
                return true;
            } catch (IOException e3) {
                e2 = e3;
                fw = fw2;
                try {
                    e2.printStackTrace();
                    if (fw != null) {
                    }
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    if (fw != null) {
                        try {
                            fw.close();
                        } catch (IOException e22) {
                            e22.printStackTrace();
                            return false;
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fw = fw2;
                if (fw != null) {
                }
                throw th;
            }
        } catch (IOException e4) {
            e22 = e4;
            e22.printStackTrace();
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e222) {
                    e222.printStackTrace();
                    return false;
                }
            }
            return false;
        }
    }

    public void deleteFilesUnderDir(String dirPath, String ext, boolean delDir) {
        File file = new File(dirPath);
        if (file != null && (file.exists() ^ 1) == 0) {
            deleteFiles(file, ext, delDir);
        }
    }

    private void deleteFiles(File dir, String ext, boolean delDir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String file : children) {
                    deleteFiles(new File(dir, file), ext, delDir);
                }
                if (delDir) {
                    dir.delete();
                }
            }
        } else if (dir.isFile() && (ext.isEmpty() || dir.getName().endsWith(ext))) {
            dir.delete();
        }
    }
}
