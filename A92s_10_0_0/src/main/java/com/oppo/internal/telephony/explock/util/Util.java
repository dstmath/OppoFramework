package com.oppo.internal.telephony.explock.util;

import android.util.Log;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.UnsupportedEncodingException;

public final class Util {
    public static final int DATA_LENGTH = 16;
    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    public static final boolean IS_DEBUG_ENV = false;
    public static final int MIN_LENGTH = 0;
    private static final String TAG = "Util";

    private Util() {
    }

    public static byte[] getUTF8Bytes(String string) {
        byte[] buff;
        StringBuilder sb;
        if (string == null) {
            return new byte[0];
        }
        try {
            return string.getBytes("UTF-8");
        } catch (UnsupportedEncodingException ue) {
            Log.w(TAG, "getUTF8Bytes UnsupportedEncodingException ue = " + ue);
            ByteArrayOutputStream bos = null;
            DataOutputStream dos = null;
            try {
                bos = new ByteArrayOutputStream();
                dos = new DataOutputStream(bos);
                dos.writeUTF(string);
                byte[] jdata = bos.toByteArray();
                buff = new byte[(jdata.length - 2)];
                System.arraycopy(jdata, 2, buff, 0, buff.length);
                try {
                    bos.close();
                } catch (Exception e) {
                    Log.e(TAG, "getUTF8Bytes bos close e = " + e);
                }
                try {
                    dos.close();
                } catch (Exception e2) {
                    e = e2;
                    sb = new StringBuilder();
                }
            } catch (Exception e3) {
                buff = new byte[0];
                if (bos != null) {
                    try {
                        bos.close();
                    } catch (Exception e4) {
                        Log.e(TAG, "getUTF8Bytes bos close e = " + e4);
                    }
                }
                if (dos != null) {
                    try {
                        dos.close();
                    } catch (Exception e5) {
                        e = e5;
                        sb = new StringBuilder();
                    }
                }
                return buff;
            } catch (Throwable th) {
                if (bos != null) {
                    try {
                        bos.close();
                    } catch (Exception e6) {
                        Log.e(TAG, "getUTF8Bytes bos close e = " + e6);
                    }
                }
                if (dos != null) {
                    try {
                        dos.close();
                    } catch (Exception e7) {
                        Log.e(TAG, "getUTF8Bytes dos close e = " + e7);
                    }
                }
                throw th;
            }
            return buff;
        }
        sb.append("getUTF8Bytes dos close e = ");
        sb.append(e);
        Log.e(TAG, sb.toString());
        return buff;
    }

    public static String encode(byte[] binaryData) {
        if (binaryData.length != 16) {
            return null;
        }
        char[] buffer = new char[32];
        for (int i = 0; i < 16; i++) {
            char[] cArr = HEX_DIGITS;
            buffer[i * 2] = cArr[(binaryData[i] & 240) >> 4];
            buffer[(i * 2) + 1] = cArr[binaryData[i] & 15];
        }
        return new String(buffer);
    }

    public static String encodeWithUtf8(byte[] binaryData) {
        try {
            return new String(binaryData, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "encodeWithUtf8 e = " + e);
            return null;
        }
    }

    public static byte[] intToByteArray(int n) {
        StringBuilder sb;
        byte[] byteArray = null;
        DataOutputStream dataOut = null;
        ByteArrayOutputStream byteOut = null;
        try {
            byteOut = new ByteArrayOutputStream();
            dataOut = new DataOutputStream(byteOut);
            dataOut.writeInt(n);
            byteArray = byteOut.toByteArray();
            try {
                dataOut.close();
            } catch (Exception e) {
                Log.e(TAG, "intToByteArray dataOut close e = " + e);
            }
            try {
                byteOut.close();
            } catch (Exception e2) {
                e = e2;
                sb = new StringBuilder();
            }
        } catch (Exception e3) {
            Log.e(TAG, "intToByteArray e = " + e3);
            if (dataOut != null) {
                try {
                    dataOut.close();
                } catch (Exception e4) {
                    Log.e(TAG, "intToByteArray dataOut close e = " + e4);
                }
            }
            if (byteOut != null) {
                try {
                    byteOut.close();
                } catch (Exception e5) {
                    e = e5;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (dataOut != null) {
                try {
                    dataOut.close();
                } catch (Exception e6) {
                    Log.e(TAG, "intToByteArray dataOut close e = " + e6);
                }
            }
            if (byteOut != null) {
                try {
                    byteOut.close();
                } catch (Exception e7) {
                    Log.e(TAG, "intToByteArray byteOut close e = " + e7);
                }
            }
            throw th;
        }
        return byteArray;
        sb.append("intToByteArray byteOut close e = ");
        sb.append(e);
        Log.e(TAG, sb.toString());
        return byteArray;
    }

    public static int byteArrayToInt(byte[] byteArray) {
        StringBuilder sb;
        int n = -1;
        ByteArrayInputStream byteInput = null;
        DataInputStream dataInput = null;
        try {
            byteInput = new ByteArrayInputStream(byteArray);
            dataInput = new DataInputStream(byteInput);
            n = dataInput.readInt();
            try {
                byteInput.close();
            } catch (Exception e) {
                Log.e(TAG, "byteArrayToInt byteInput close e = " + e);
            }
            try {
                dataInput.close();
            } catch (Exception e2) {
                e = e2;
                sb = new StringBuilder();
            }
        } catch (Exception e3) {
            Log.e(TAG, "byteArrayToInt e = " + e3);
            if (byteInput != null) {
                try {
                    byteInput.close();
                } catch (Exception e4) {
                    Log.e(TAG, "byteArrayToInt byteInput close e = " + e4);
                }
            }
            if (dataInput != null) {
                try {
                    dataInput.close();
                } catch (Exception e5) {
                    e = e5;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (byteInput != null) {
                try {
                    byteInput.close();
                } catch (Exception e6) {
                    Log.e(TAG, "byteArrayToInt byteInput close e = " + e6);
                }
            }
            if (dataInput != null) {
                try {
                    dataInput.close();
                } catch (Exception e7) {
                    Log.e(TAG, "byteArrayToInt dataInput close e = " + e7);
                }
            }
            throw th;
        }
        return n;
        sb.append("byteArrayToInt dataInput close e = ");
        sb.append(e);
        Log.e(TAG, sb.toString());
        return n;
    }

    public static byte[] booleanToByteArray(boolean value) {
        StringBuilder sb;
        byte[] byteArray = null;
        ByteArrayOutputStream byteOut = null;
        DataOutputStream dataOut = null;
        try {
            byteOut = new ByteArrayOutputStream();
            dataOut = new DataOutputStream(byteOut);
            dataOut.writeBoolean(value);
            byteArray = byteOut.toByteArray();
            try {
                dataOut.close();
            } catch (Exception e) {
                Log.e(TAG, "booleanToByteArray dataOut close e = " + e);
            }
            try {
                byteOut.close();
            } catch (Exception e2) {
                e = e2;
                sb = new StringBuilder();
            }
        } catch (Exception e3) {
            Log.e(TAG, "booleanToByteArray e = " + e3);
            if (dataOut != null) {
                try {
                    dataOut.close();
                } catch (Exception e4) {
                    Log.e(TAG, "booleanToByteArray dataOut close e = " + e4);
                }
            }
            if (byteOut != null) {
                try {
                    byteOut.close();
                } catch (Exception e5) {
                    e = e5;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (dataOut != null) {
                try {
                    dataOut.close();
                } catch (Exception e6) {
                    Log.e(TAG, "booleanToByteArray dataOut close e = " + e6);
                }
            }
            if (byteOut != null) {
                try {
                    byteOut.close();
                } catch (Exception e7) {
                    Log.e(TAG, "booleanToByteArray byteOut close e = " + e7);
                }
            }
            throw th;
        }
        return byteArray;
        sb.append("booleanToByteArray byteOut close e = ");
        sb.append(e);
        Log.e(TAG, sb.toString());
        return byteArray;
    }

    public static boolean isArrayEmpty(byte[] bytes) {
        return bytes == null || bytes.length == 0;
    }
}
