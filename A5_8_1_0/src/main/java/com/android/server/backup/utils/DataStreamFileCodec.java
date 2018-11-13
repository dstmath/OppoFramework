package com.android.server.backup.utils;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public final class DataStreamFileCodec<T> {
    private final DataStreamCodec<T> mCodec;
    private final File mFile;

    public DataStreamFileCodec(File file, DataStreamCodec<T> codec) {
        this.mFile = file;
        this.mCodec = codec;
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x0038 A:{SYNTHETIC, Splitter: B:28:0x0038} */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x003e A:{SYNTHETIC, Splitter: B:32:0x003e} */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x0059  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0044  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0038 A:{SYNTHETIC, Splitter: B:28:0x0038} */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x003e A:{SYNTHETIC, Splitter: B:32:0x003e} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0044  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x0059  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public T deserialize() throws IOException {
        Throwable th;
        Throwable th2;
        Throwable th3 = null;
        FileInputStream fileInputStream = null;
        DataInputStream dataInputStream = null;
        try {
            FileInputStream fileInputStream2 = new FileInputStream(this.mFile);
            try {
                DataInputStream dataInputStream2 = new DataInputStream(fileInputStream2);
                try {
                    T deserialize = this.mCodec.deserialize(dataInputStream2);
                    if (dataInputStream2 != null) {
                        try {
                            dataInputStream2.close();
                        } catch (Throwable th4) {
                            th3 = th4;
                        }
                    }
                    if (fileInputStream2 != null) {
                        try {
                            fileInputStream2.close();
                        } catch (Throwable th5) {
                            th = th5;
                            if (th3 != null) {
                                if (th3 != th) {
                                    th3.addSuppressed(th);
                                    th = th3;
                                }
                            }
                        }
                    }
                    th = th3;
                    if (th == null) {
                        return deserialize;
                    }
                    throw th;
                } catch (Throwable th6) {
                    th = th6;
                    dataInputStream = dataInputStream2;
                    fileInputStream = fileInputStream2;
                    if (dataInputStream != null) {
                    }
                    th2 = th3;
                    if (fileInputStream != null) {
                    }
                    th3 = th2;
                    if (th3 != null) {
                    }
                }
            } catch (Throwable th7) {
                th = th7;
                fileInputStream = fileInputStream2;
                if (dataInputStream != null) {
                    try {
                        dataInputStream.close();
                    } catch (Throwable th8) {
                        th2 = th8;
                        if (th3 != null) {
                            if (th3 != th2) {
                                th3.addSuppressed(th2);
                                th2 = th3;
                            }
                        }
                    }
                }
                th2 = th3;
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (Throwable th9) {
                        th3 = th9;
                        if (th2 != null) {
                            if (th2 != th3) {
                                th2.addSuppressed(th3);
                                th3 = th2;
                            }
                        }
                    }
                }
                th3 = th2;
                if (th3 != null) {
                    throw th3;
                }
                throw th;
            }
        } catch (Throwable th10) {
            th = th10;
            if (dataInputStream != null) {
            }
            th2 = th3;
            if (fileInputStream != null) {
            }
            th3 = th2;
            if (th3 != null) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:38:0x0050 A:{SYNTHETIC, Splitter: B:38:0x0050} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0055 A:{SYNTHETIC, Splitter: B:41:0x0055} */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x005b A:{SYNTHETIC, Splitter: B:45:0x005b} */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x0081  */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x0061  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0050 A:{SYNTHETIC, Splitter: B:38:0x0050} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0055 A:{SYNTHETIC, Splitter: B:41:0x0055} */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x005b A:{SYNTHETIC, Splitter: B:45:0x005b} */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x0061  */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x0081  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0050 A:{SYNTHETIC, Splitter: B:38:0x0050} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0055 A:{SYNTHETIC, Splitter: B:41:0x0055} */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x005b A:{SYNTHETIC, Splitter: B:45:0x005b} */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x0081  */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x0061  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void serialize(T t) throws IOException {
        Throwable th;
        Throwable th2;
        Throwable th3 = null;
        FileOutputStream fileOutputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        DataOutputStream dataOutputStream = null;
        try {
            BufferedOutputStream bufferedOutputStream2;
            FileOutputStream fileOutputStream2 = new FileOutputStream(this.mFile);
            try {
                bufferedOutputStream2 = new BufferedOutputStream(fileOutputStream2);
            } catch (Throwable th4) {
                th = th4;
                fileOutputStream = fileOutputStream2;
                if (dataOutputStream != null) {
                }
                if (bufferedOutputStream != null) {
                }
                th2 = th3;
                if (fileOutputStream != null) {
                }
                th3 = th2;
                if (th3 == null) {
                }
            }
            try {
                DataOutputStream dataOutputStream2 = new DataOutputStream(bufferedOutputStream2);
                try {
                    this.mCodec.serialize(t, dataOutputStream2);
                    dataOutputStream2.flush();
                    if (dataOutputStream2 != null) {
                        try {
                            dataOutputStream2.close();
                        } catch (Throwable th5) {
                            th = th5;
                        }
                    }
                    th = null;
                    if (bufferedOutputStream2 != null) {
                        try {
                            bufferedOutputStream2.close();
                        } catch (Throwable th6) {
                            th3 = th6;
                            if (th != null) {
                                if (th != th3) {
                                    th.addSuppressed(th3);
                                    th3 = th;
                                }
                            }
                        }
                    }
                    th3 = th;
                    if (fileOutputStream2 != null) {
                        try {
                            fileOutputStream2.close();
                        } catch (Throwable th7) {
                            th = th7;
                            if (th3 != null) {
                                if (th3 != th) {
                                    th3.addSuppressed(th);
                                    th = th3;
                                }
                            }
                        }
                    }
                    th = th3;
                    if (th != null) {
                        throw th;
                    }
                } catch (Throwable th8) {
                    th = th8;
                    dataOutputStream = dataOutputStream2;
                    bufferedOutputStream = bufferedOutputStream2;
                    fileOutputStream = fileOutputStream2;
                    if (dataOutputStream != null) {
                    }
                    if (bufferedOutputStream != null) {
                    }
                    th2 = th3;
                    if (fileOutputStream != null) {
                    }
                    th3 = th2;
                    if (th3 == null) {
                    }
                }
            } catch (Throwable th9) {
                th = th9;
                bufferedOutputStream = bufferedOutputStream2;
                fileOutputStream = fileOutputStream2;
                if (dataOutputStream != null) {
                }
                if (bufferedOutputStream != null) {
                }
                th2 = th3;
                if (fileOutputStream != null) {
                }
                th3 = th2;
                if (th3 == null) {
                }
            }
        } catch (Throwable th10) {
            th = th10;
            if (dataOutputStream != null) {
                try {
                    dataOutputStream.close();
                } catch (Throwable th22) {
                    if (th3 == null) {
                        th3 = th22;
                    } else if (th3 != th22) {
                        th3.addSuppressed(th22);
                    }
                }
            }
            if (bufferedOutputStream != null) {
                try {
                    bufferedOutputStream.close();
                } catch (Throwable th11) {
                    th22 = th11;
                    if (th3 != null) {
                        if (th3 != th22) {
                            th3.addSuppressed(th22);
                            th22 = th3;
                        }
                    }
                }
            }
            th22 = th3;
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (Throwable th12) {
                    th3 = th12;
                    if (th22 != null) {
                        if (th22 != th3) {
                            th22.addSuppressed(th3);
                            th3 = th22;
                        }
                    }
                }
            }
            th3 = th22;
            if (th3 == null) {
                throw th3;
            }
            throw th;
        }
    }
}
