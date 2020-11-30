package com.oppo.internal.telephony.explock.util;

import android.telephony.Rlog;
import java.util.Arrays;
import java.util.HashMap;

/* access modifiers changed from: package-private */
public class RpmbResultParser {
    private static final boolean DBG = false;
    private static final int INT_LEN = 4;
    private static final int RESULT_HEAD_LEN = 12;
    private static final int RESULT_PARAM_HEAD_LEN = 8;
    private static final String TAG = "RpmbResultParser";

    RpmbResultParser() {
    }

    static ResultSummary parse(byte[] buffer) {
        ResultSummary resultSummary = null;
        if (Util.isArrayEmpty(buffer)) {
            Rlog.e(TAG, "buffer is empty");
            return null;
        }
        ResultSummary resultSummary2 = new ResultSummary();
        int totalBufferNum = buffer.length;
        if (totalBufferNum >= 12) {
            byte[] methodTypeBytes = Arrays.copyOfRange(buffer, 0, 4);
            if (!Util.isArrayEmpty(methodTypeBytes)) {
                int methodTypeCode = Util.byteArrayToInt(methodTypeBytes);
                int hasParsedBytesNum = 0 + 4;
                if (methodTypeCode > 0) {
                    MethodType methodType = MethodType.get(methodTypeCode);
                    if (methodType != null) {
                        resultSummary2.setMethodType(methodType);
                        byte[] isExeSuccessBytes = Arrays.copyOfRange(buffer, 4, 8);
                        if (!Util.isArrayEmpty(isExeSuccessBytes)) {
                            int hasParsedBytesNum2 = hasParsedBytesNum + 4;
                            if (Util.byteArrayToInt(isExeSuccessBytes) != 0) {
                                return resultSummary2;
                            }
                            resultSummary2.setIsExeSuccess(true);
                            byte[] paramsNumBytes = Arrays.copyOfRange(buffer, 8, 12);
                            if (!Util.isArrayEmpty(paramsNumBytes)) {
                                int paramsNum = Util.byteArrayToInt(paramsNumBytes);
                                int hasParsedBytesNum3 = hasParsedBytesNum2 + 4;
                                Rlog.d(TAG, "parse paramsNum = " + paramsNum);
                                if (paramsNum > 0) {
                                    resultSummary2.setParamsNum(paramsNum);
                                    int i = 0;
                                    while (i < paramsNum) {
                                        if (hasParsedBytesNum3 + 8 < totalBufferNum) {
                                            ResultParam resultParam = new ResultParam();
                                            byte[] methodParamsTypeBytes = Arrays.copyOfRange(buffer, hasParsedBytesNum3, hasParsedBytesNum3 + 4);
                                            if (!Util.isArrayEmpty(methodParamsTypeBytes)) {
                                                MethodParamType methodParamType = MethodParamType.get(Util.byteArrayToInt(methodParamsTypeBytes));
                                                Rlog.d(TAG, "parse i = " + i + ", methodParamType = " + methodParamType);
                                                if (methodParamType != null) {
                                                    resultParam.setMethodParamType(methodParamType);
                                                    hasParsedBytesNum3 += 4;
                                                    byte[] bufferLenBytes = Arrays.copyOfRange(buffer, hasParsedBytesNum3, hasParsedBytesNum3 + 4);
                                                    if (!Util.isArrayEmpty(bufferLenBytes)) {
                                                        int bufferLen = Util.byteArrayToInt(bufferLenBytes);
                                                        Rlog.d(TAG, "parse i = " + i + ", bufferLen = " + bufferLen);
                                                        if (bufferLen > 0) {
                                                            resultParam.setBufferLen(bufferLen);
                                                            int hasParsedBytesNum4 = hasParsedBytesNum3 + 4;
                                                            int bufferlen = resultParam.getBufferLen();
                                                            if (hasParsedBytesNum4 + bufferlen <= totalBufferNum) {
                                                                byte[] resultBuffer = Arrays.copyOfRange(buffer, hasParsedBytesNum4, hasParsedBytesNum4 + bufferlen);
                                                                if (!Util.isArrayEmpty(resultBuffer)) {
                                                                    resultParam.setBuffer(resultBuffer);
                                                                    resultSummary2.addResultParam(resultParam);
                                                                    hasParsedBytesNum3 = hasParsedBytesNum4 + bufferlen;
                                                                } else {
                                                                    Rlog.e(TAG, "parse resultBuffer is empty i = " + i);
                                                                    return null;
                                                                }
                                                            } else {
                                                                Rlog.e(TAG, "parse bufferLenBytes is empty i = " + i + ", hasParsedBytesNum = " + hasParsedBytesNum4 + ", bufferlen = " + bufferlen + ", totalBufferNum = " + totalBufferNum);
                                                                return null;
                                                            }
                                                        } else {
                                                            Rlog.w(TAG, "parse bufferLen is invalid, i = " + i);
                                                        }
                                                        i++;
                                                        paramsNumBytes = paramsNumBytes;
                                                        methodTypeBytes = methodTypeBytes;
                                                        isExeSuccessBytes = isExeSuccessBytes;
                                                        resultSummary = null;
                                                    } else {
                                                        Rlog.e(TAG, "parse bufferLenBytes is empty i = " + i);
                                                        return null;
                                                    }
                                                } else {
                                                    Rlog.e(TAG, "parse methodParamType is null, i = " + i);
                                                    return null;
                                                }
                                            } else {
                                                Rlog.e(TAG, "parse methodParamsTypeBytes is empty i = " + i);
                                                return resultSummary;
                                            }
                                        } else {
                                            Rlog.e(TAG, "parse invalid (hasParsedBytesNum + RESULT_PARAM_HEAD_LEN) = " + (hasParsedBytesNum3 + 8) + ", totalBufferNum = " + totalBufferNum);
                                            return null;
                                        }
                                    }
                                } else {
                                    Rlog.w(TAG, "parse invalid paramsNum = " + paramsNum);
                                }
                            } else {
                                Rlog.e(TAG, "parse paramsNumBytes is empty");
                                return null;
                            }
                        } else {
                            Rlog.e(TAG, "parse isExeSuccessBytes is empty");
                            return null;
                        }
                    } else {
                        Rlog.e(TAG, "parse invalid methodType is null");
                        return null;
                    }
                } else {
                    Rlog.e(TAG, "parse invalid methodTypeCode = " + methodTypeCode);
                    return null;
                }
            } else {
                Rlog.e(TAG, "parse methodTypeBytes is empty");
                return null;
            }
        }
        return resultSummary2;
    }

    /* access modifiers changed from: package-private */
    public static class ResultSummary {
        private boolean mIsExeSuccess = false;
        private MethodType mMethodType = null;
        private int mParamsNum = 0;
        private HashMap<MethodParamType, ResultParam> mResultParamMap = new HashMap<>();

        ResultSummary() {
        }

        /* access modifiers changed from: package-private */
        public void setMethodType(MethodType type) {
            this.mMethodType = type;
        }

        /* access modifiers changed from: package-private */
        public MethodType getMethodType() {
            return this.mMethodType;
        }

        /* access modifiers changed from: package-private */
        public boolean isExeSuccess() {
            return this.mIsExeSuccess;
        }

        /* access modifiers changed from: package-private */
        public void setIsExeSuccess(boolean isExeSuccess) {
            this.mIsExeSuccess = isExeSuccess;
        }

        /* access modifiers changed from: package-private */
        public void setParamsNum(int num) {
            this.mParamsNum = num;
        }

        /* access modifiers changed from: package-private */
        public int getParamsNum() {
            return this.mParamsNum;
        }

        /* access modifiers changed from: package-private */
        public void addResultParam(ResultParam param) {
            if (param != null && param.getMethodParamType() != null) {
                this.mResultParamMap.put(param.getMethodParamType(), param);
            }
        }

        /* access modifiers changed from: package-private */
        public HashMap<MethodParamType, ResultParam> getResultParamMap() {
            return this.mResultParamMap;
        }

        /* access modifiers changed from: package-private */
        public ResultParam getResultParam(MethodParamType type) {
            HashMap<MethodParamType, ResultParam> hashMap;
            if (type == null || (hashMap = this.mResultParamMap) == null) {
                return null;
            }
            return hashMap.get(type);
        }
    }

    /* access modifiers changed from: package-private */
    public static class ResultParam {
        private byte[] mBuffer = null;
        private int mBufferLen = 0;
        private MethodParamType mMethodParamType = null;

        ResultParam() {
        }

        /* access modifiers changed from: package-private */
        public void setMethodParamType(MethodParamType type) {
            this.mMethodParamType = type;
        }

        /* access modifiers changed from: package-private */
        public MethodParamType getMethodParamType() {
            return this.mMethodParamType;
        }

        /* access modifiers changed from: package-private */
        public void setBufferLen(int len) {
            this.mBufferLen = len;
        }

        /* access modifiers changed from: package-private */
        public int getBufferLen() {
            return this.mBufferLen;
        }

        /* access modifiers changed from: package-private */
        public void setBuffer(byte[] bytes) {
            this.mBuffer = bytes;
        }

        /* access modifiers changed from: package-private */
        public byte[] getBuffer() {
            return this.mBuffer;
        }
    }
}
