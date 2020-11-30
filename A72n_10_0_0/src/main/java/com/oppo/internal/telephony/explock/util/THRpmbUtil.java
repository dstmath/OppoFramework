package com.oppo.internal.telephony.explock.util;

import android.content.Context;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.OemConstant;
import com.oppo.internal.telephony.explock.OemLockUtils;
import com.oppo.internal.telephony.explock.util.RpmbResultParser;

public final class THRpmbUtil {
    private static final int CHECK_MOVED_DATA_MAX = 2;
    private static final boolean DBG = OemConstant.SWITCH_LOG;
    private static final int RPMB_DATA_SIZE = 128;
    private static final String TAG = "THRpmbUtil";
    private static int sCheckHasMovedDataToRpmbCount = 0;
    private static boolean sHasMovedDataToRpmb = false;

    private THRpmbUtil() {
    }

    public static boolean hasMovedDataToRpmb() {
        if (DBG) {
            Rlog.d(TAG, "hasMovedDataToRpmb");
        }
        if (sCheckHasMovedDataToRpmbCount >= 2) {
            Rlog.d(TAG, "hasMovedDataToRpmb has check max count and return false");
            return false;
        }
        ValueResult valueResult = hasMovedDataToRpmbValue();
        if (valueResult == null || !((Boolean) valueResult.getDeviceValue()).booleanValue()) {
            return false;
        }
        return true;
    }

    protected static ValueResult hasMovedDataToRpmbValue() {
        ValueResult valueResult = new ValueResult();
        if (sHasMovedDataToRpmb) {
            valueResult.setGetValueSuccess(true);
            valueResult.setDeviceValue(true);
            return valueResult;
        }
        MethodBuffer methodBuffer = new MethodBuffer(MethodType.readSimLockDataFromRPMB);
        methodBuffer.appendStringParam(MethodParamType.backSimLockData, MethodParamType.backSimLockData.name());
        try {
            RpmbResultParser.ResultSummary resultSummary = RpmbResultParser.parse(RpmbChannel.getInstance().processCmdV2(methodBuffer.buildBuffer()));
            if (isMethodExecuteSuccess(resultSummary, MethodType.readSimLockDataFromRPMB)) {
                valueResult.setGetValueSuccess(true);
                RpmbResultParser.ResultParam resultParam = resultSummary.getResultParam(MethodParamType.backSimLockData);
                if (resultParam != null) {
                    byte[] resultParamBuffer = resultParam.getBuffer();
                    if (!Util.isArrayEmpty(resultParamBuffer)) {
                        String result = String.valueOf(Util.byteArrayToInt(resultParamBuffer));
                        String readValue = Util.encodeWithUtf8(resultParamBuffer);
                        String[] parseResult = parseRpmbDataValues(readValue);
                        if (parseResult == null || parseResult.length != 9) {
                            sHasMovedDataToRpmb = false;
                        } else {
                            String operator = parseResult[0];
                            if (TextUtils.isEmpty(operator) || (!"AIS".equals(operator) && !"TRUE".equals(operator) && !"DTAC".equals(operator))) {
                                sHasMovedDataToRpmb = false;
                            } else {
                                sHasMovedDataToRpmb = true;
                            }
                        }
                        if (DBG) {
                            Rlog.d(TAG, "hasMovedDataToRpmbValue result = " + result + "\nreadValue = " + readValue + "\nsHasMovedDataToRpmb = " + sHasMovedDataToRpmb);
                        }
                        if (!sHasMovedDataToRpmb) {
                            sCheckHasMovedDataToRpmbCount++;
                        } else {
                            sCheckHasMovedDataToRpmbCount = 0;
                        }
                        valueResult.setDeviceValue(Boolean.valueOf(sHasMovedDataToRpmb));
                        return valueResult;
                    }
                    Rlog.e(TAG, "hasMovedDataToRpmbValue resultParamBuffer is empty");
                } else {
                    Rlog.e(TAG, "hasMovedDataToRpmbValue resultParam is null");
                }
                return null;
            }
            Rlog.e(TAG, "hasMovedDataToRpmbValue isMethodExecuteSuccess is false");
            return null;
        } catch (Exception e) {
            Rlog.e(TAG, "hasMovedDataToRpmbValue e = " + e);
            return null;
        }
    }

    protected static synchronized boolean moveDataToRpmb(boolean isLockEnabled, boolean forceUpdate) {
        synchronized (THRpmbUtil.class) {
            ValueResult hasMovedDataToRpmbValue = hasMovedDataToRpmbValue();
            if (hasMovedDataToRpmbValue != null) {
                if (hasMovedDataToRpmbValue.isGetValueSuccess()) {
                    Object value = hasMovedDataToRpmbValue.getDeviceValue();
                    if (!(value instanceof Boolean) || (((Boolean) value).booleanValue() && !forceUpdate)) {
                        Rlog.e(TAG, "moveDataToRpmb hasMovedDataToRpmb is true");
                    } else {
                        DeviceLockData rpmbData = OemLockUtils.getDeviceLockDataToRpmb(true);
                        if (rpmbData == null) {
                            Rlog.e(TAG, "moveDataToRpmb rpmbData is null");
                            return false;
                        }
                        String rpmbDataStr = rpmbData.toString();
                        if (TextUtils.isEmpty(rpmbDataStr)) {
                            return false;
                        }
                        MethodBuffer methodBuffer = new MethodBuffer(MethodType.writeSimLockDataFromRPMB);
                        String rpmbDataStr2 = buildVaildLockDataString(rpmbDataStr);
                        if (DBG) {
                            Rlog.d(TAG, "moveDataToRpmb rpmbData = " + rpmbDataStr2.toString() + "length " + rpmbDataStr2.length());
                        }
                        methodBuffer.appendStringParam(MethodParamType.backSimLockData, rpmbDataStr2);
                        try {
                            if (isMethodExecuteSuccess(RpmbResultParser.parse(RpmbChannel.getInstance().processCmdV2(methodBuffer.buildBuffer())), MethodType.writeSimLockDataFromRPMB)) {
                                Rlog.d(TAG, "moveDataToRpmb success");
                                return true;
                            }
                            Rlog.e(TAG, "moveDataToRpmb isMethodExecuteSuccess is false");
                        } catch (Exception e) {
                            Rlog.e(TAG, "moveDataToRpmb e = " + e);
                        }
                    }
                    return false;
                }
            }
            Rlog.w(TAG, "moveDataToRpmb hasMovedDataToRpmbValue = " + hasMovedDataToRpmbValue);
            return false;
        }
    }

    public static void checkMoveDataToRpmb() {
        if (hasMovedDataToRpmb()) {
            return;
        }
        if (moveDataToRpmb(true, false)) {
            Rlog.d(TAG, "checkMoveDataToRpmb moveDataToRpmb success!");
        } else {
            Rlog.e(TAG, "checkMoveDataToRpmb moveDataToRpmb failed!");
        }
    }

    protected static String getResultParamValue(RpmbResultParser.ResultParam resultParam, boolean isResultInt) {
        if (resultParam == null) {
            return null;
        }
        byte[] resultParamBuffer = resultParam.getBuffer();
        if (Util.isArrayEmpty(resultParamBuffer)) {
            return null;
        }
        if (isResultInt) {
            return String.valueOf(Util.byteArrayToInt(resultParamBuffer));
        }
        return Util.encodeWithUtf8(resultParamBuffer);
    }

    protected static boolean isMethodExecuteSuccess(RpmbResultParser.ResultSummary resultSummary, MethodType methodType) {
        Rlog.d(TAG, "isMethodExecuteSuccess methodType = " + methodType);
        if (resultSummary == null) {
            Rlog.e(TAG, "isMethodExecuteSuccess resultSummary is null");
            return false;
        } else if (methodType == null) {
            Rlog.e(TAG, "isMethodExecuteSuccess methodType is null");
            return false;
        } else {
            MethodType resultMethodType = resultSummary.getMethodType();
            if (resultMethodType == null) {
                Rlog.e(TAG, "isMethodExecuteSuccess resultMethodType is null");
                return false;
            } else if (resultMethodType.getCode() != methodType.getCode()) {
                Rlog.e(TAG, "isMethodExecuteSuccess resultMethodType = " + resultMethodType + ", methodType = " + methodType);
                return false;
            } else if (resultSummary.isExeSuccess()) {
                return true;
            } else {
                Rlog.e(TAG, "isMethodExecuteSuccess isExeSuccess = false");
                return false;
            }
        }
    }

    protected static String[] parseRpmbDataValues(String rpmbValue) {
        String[] lockData;
        String[] data = new String[9];
        if (TextUtils.isEmpty(rpmbValue) || rpmbValue.length() != 128 || (lockData = rpmbValue.split("\\$")) == null || lockData.length != 9) {
            return null;
        }
        for (int i = 0; i < 9; i++) {
            data[i] = lockData[i];
        }
        return data;
    }

    protected static synchronized boolean initDefaultDataToRpmb(Context context) {
        synchronized (THRpmbUtil.class) {
            if (DBG) {
                Rlog.d(TAG, "initDefaultDataToRpmb");
            }
            String defaultData = "00000000";
            for (int i = defaultData.length(); i < 128; i++) {
                defaultData = defaultData + "0";
            }
            if (DBG) {
                Rlog.d(TAG, "initDefaultDataToRpmb defaultData = " + defaultData.toString() + "length = " + defaultData.length());
            }
            MethodBuffer methodBuffer = new MethodBuffer(MethodType.writeSimLockDataFromRPMB);
            methodBuffer.appendStringParam(MethodParamType.backSimLockData, defaultData);
            try {
                if (isMethodExecuteSuccess(RpmbResultParser.parse(RpmbChannel.getInstance().processCmdV2(methodBuffer.buildBuffer())), MethodType.writeSimLockDataFromRPMB)) {
                    Rlog.d(TAG, "initDefaultDataToRpmb success");
                    return true;
                }
                Rlog.e(TAG, "initDefaultDataToRpmb isMethodExecuteSuccess is false");
                return false;
            } catch (Exception e) {
                Rlog.e(TAG, "initDefaultDataToRpmb e = " + e);
            }
        }
    }

    protected static synchronized String getRpmbLockDataString() {
        synchronized (THRpmbUtil.class) {
            if (!hasMovedDataToRpmb()) {
                Rlog.e(TAG, "getRpmbLockDataString result null");
                return null;
            }
            ValueResult valueResult = new ValueResult();
            MethodBuffer methodBuffer = new MethodBuffer(MethodType.readSimLockDataFromRPMB);
            methodBuffer.appendStringParam(MethodParamType.backSimLockData, MethodParamType.backSimLockData.name());
            try {
                RpmbResultParser.ResultSummary resultSummary = RpmbResultParser.parse(RpmbChannel.getInstance().processCmdV2(methodBuffer.buildBuffer()));
                if (isMethodExecuteSuccess(resultSummary, MethodType.readSimLockDataFromRPMB)) {
                    valueResult.setGetValueSuccess(true);
                    RpmbResultParser.ResultParam resultParam = resultSummary.getResultParam(MethodParamType.backSimLockData);
                    if (resultParam != null) {
                        byte[] resultParamBuffer = resultParam.getBuffer();
                        if (!Util.isArrayEmpty(resultParamBuffer)) {
                            String.valueOf(Util.byteArrayToInt(resultParamBuffer));
                            String readValue = Util.encodeWithUtf8(resultParamBuffer);
                            String[] parseResult = parseRpmbDataValues(readValue);
                            if (parseResult != null && parseResult.length == 9) {
                                Rlog.e(TAG, "getRpmbLockDataString result readValue = " + readValue);
                                return readValue;
                            }
                        } else {
                            Rlog.e(TAG, "getRpmbLockDataString resultParamBuffer is empty");
                        }
                    } else {
                        Rlog.e(TAG, "getRpmbLockDataString resultParam is null");
                    }
                } else {
                    Rlog.e(TAG, "getRpmbLockDataString isMethodExecuteSuccess is false");
                }
            } catch (Exception e) {
            }
            return null;
        }
    }

    public static String getRpmbOperatorData() {
        String[] parseResult = parseRpmbDataValues(getRpmbLockDataString());
        if (parseResult == null || parseResult.length != 9) {
            return null;
        }
        if ("AIS".equals(parseResult[LockDataType.dLockOperator.getCode()]) || "TRUE".equals(parseResult[LockDataType.dLockOperator.getCode()]) || "DTAC".equals(parseResult[LockDataType.dLockOperator.getCode()])) {
            return parseResult[LockDataType.dLockOperator.getCode()];
        }
        return null;
    }

    public static String getRpmbLockStatus() {
        String[] parseResult = parseRpmbDataValues(getRpmbLockDataString());
        if (parseResult == null || parseResult.length != 9) {
            return null;
        }
        return parseResult[LockDataType.dLockStatus.getCode()];
    }

    protected static boolean setRpmbLockStatus(String status) {
        String[] updateParseResult;
        String[] parseResult = parseRpmbDataValues(getRpmbLockDataString());
        if (parseResult != null && parseResult.length == 9) {
            parseResult[LockDataType.dLockStatus.getCode()] = status;
        }
        if (!updateLockDataToRpmb(buildStringLockDataToRpmb(parseResult)) || (updateParseResult = parseRpmbDataValues(getRpmbLockDataString())) == null || updateParseResult.length != 9 || !status.equals(updateParseResult[LockDataType.dLockStatus.getCode()])) {
            return false;
        }
        return true;
    }

    protected static String getRpmbLockIMSI() {
        String[] parseResult = parseRpmbDataValues(getRpmbLockDataString());
        if (parseResult == null || parseResult.length != 9) {
            return null;
        }
        return parseResult[LockDataType.dLockImsi.getCode()];
    }

    protected static boolean setRpmbLockIMSI(String imsi) {
        String[] updateParseResult;
        String[] parseResult = parseRpmbDataValues(getRpmbLockDataString());
        if (parseResult != null && parseResult.length == 9) {
            parseResult[LockDataType.dLockImsi.getCode()] = imsi;
        }
        if (!updateLockDataToRpmb(buildStringLockDataToRpmb(parseResult)) || (updateParseResult = parseRpmbDataValues(getRpmbLockDataString())) == null || updateParseResult.length != 9 || !imsi.equals(updateParseResult[LockDataType.dLockImsi.getCode()])) {
            return false;
        }
        return true;
    }

    protected static String getRpmbLockIccid() {
        String[] parseResult = parseRpmbDataValues(getRpmbLockDataString());
        if (parseResult == null || parseResult.length != 9) {
            return null;
        }
        return parseResult[LockDataType.dLockIccid.getCode()];
    }

    protected static boolean setRpmbLockIccid(String iccid) {
        String[] updateParseResult;
        String[] parseResult = parseRpmbDataValues(getRpmbLockDataString());
        if (parseResult != null && parseResult.length == 9) {
            parseResult[LockDataType.dLockIccid.getCode()] = iccid;
        }
        if (!updateLockDataToRpmb(buildStringLockDataToRpmb(parseResult)) || (updateParseResult = parseRpmbDataValues(getRpmbLockDataString())) == null || updateParseResult.length != 9 || !iccid.equals(updateParseResult[LockDataType.dLockIccid.getCode()])) {
            return false;
        }
        return true;
    }

    protected static String getRpmbLockDays() {
        String[] parseResult = parseRpmbDataValues(getRpmbLockDataString());
        if (parseResult == null || parseResult.length != 9) {
            return null;
        }
        return parseResult[LockDataType.dLockContractDays.getCode()];
    }

    protected static boolean setRpmbLockDays(String days) {
        String[] updateParseResult;
        String[] parseResult = parseRpmbDataValues(getRpmbLockDataString());
        if (parseResult != null && parseResult.length == 9) {
            parseResult[LockDataType.dLockContractDays.getCode()] = days;
        }
        if (!updateLockDataToRpmb(buildStringLockDataToRpmb(parseResult)) || (updateParseResult = parseRpmbDataValues(getRpmbLockDataString())) == null || updateParseResult.length != 9 || !days.equals(updateParseResult[LockDataType.dLockContractDays.getCode()])) {
            return false;
        }
        return true;
    }

    protected static String getRpmbLockFirstBindTime() {
        String[] parseResult = parseRpmbDataValues(getRpmbLockDataString());
        if (parseResult == null || parseResult.length != 9) {
            return null;
        }
        if ("AIS".equals(parseResult[LockDataType.dLockOperator.getCode()]) || "TRUE".equals(parseResult[LockDataType.dLockOperator.getCode()]) || "DTAC".equals(parseResult[LockDataType.dLockOperator.getCode()])) {
            return parseResult[LockDataType.dLockFirstBindTime.getCode()];
        }
        return null;
    }

    protected static boolean setRpmbLockFirstBindTime(String times) {
        String[] updateParseResult;
        String[] parseResult = parseRpmbDataValues(getRpmbLockDataString());
        if (parseResult != null && parseResult.length == 9) {
            parseResult[LockDataType.dLockFirstBindTime.getCode()] = times;
        }
        if (!updateLockDataToRpmb(buildStringLockDataToRpmb(parseResult)) || (updateParseResult = parseRpmbDataValues(getRpmbLockDataString())) == null || updateParseResult.length != 9 || !times.equals(updateParseResult[LockDataType.dLockFirstBindTime.getCode()])) {
            return false;
        }
        return true;
    }

    protected static String getRpmbLockLastBindTime() {
        String[] parseResult = parseRpmbDataValues(getRpmbLockDataString());
        if (parseResult == null || parseResult.length != 9) {
            return null;
        }
        return parseResult[LockDataType.dLockLastBindTime.getCode()];
    }

    protected static boolean setRpmbLockLastBindTime(String times) {
        String[] updateParseResult;
        String[] parseResult = parseRpmbDataValues(getRpmbLockDataString());
        if (parseResult != null && parseResult.length == 9) {
            parseResult[LockDataType.dLockLastBindTime.getCode()] = times;
        }
        if (!updateLockDataToRpmb(buildStringLockDataToRpmb(parseResult)) || (updateParseResult = parseRpmbDataValues(getRpmbLockDataString())) == null || updateParseResult.length != 9 || !times.equals(updateParseResult[LockDataType.dLockLastBindTime.getCode()])) {
            return false;
        }
        return true;
    }

    protected static String getRpmbLockUnlockTime() {
        String[] parseResult = parseRpmbDataValues(getRpmbLockDataString());
        if (parseResult == null || parseResult.length != 9) {
            return null;
        }
        return parseResult[LockDataType.dLockUnlockDate.getCode()];
    }

    protected static boolean setRpmbLockUnlockTime(String time) {
        String[] updateParseResult;
        String[] parseResult = parseRpmbDataValues(getRpmbLockDataString());
        if (parseResult != null && parseResult.length == 9) {
            parseResult[LockDataType.dLockUnlockDate.getCode()] = time;
        }
        if (!updateLockDataToRpmb(buildStringLockDataToRpmb(parseResult)) || (updateParseResult = parseRpmbDataValues(getRpmbLockDataString())) == null || updateParseResult.length != 9 || !time.equals(updateParseResult[LockDataType.dLockUnlockDate.getCode()])) {
            return false;
        }
        return true;
    }

    protected static String buildStringLockDataToRpmb(String[] data) {
        if (data == null || data.length != 9) {
            return null;
        }
        DeviceLockData rpmbData = new DeviceLockData();
        rpmbData.setLockedOperator(data[LockDataType.dLockOperator.getCode()]);
        rpmbData.setLockedState(data[LockDataType.dLockStatus.getCode()]);
        rpmbData.setLockedIMSI(data[LockDataType.dLockImsi.getCode()]);
        rpmbData.setContractDays(data[LockDataType.dLockContractDays.getCode()]);
        rpmbData.setFirstBindTime(data[LockDataType.dLockFirstBindTime.getCode()]);
        rpmbData.setLockedICCID(data[LockDataType.dLockIccid.getCode()]);
        rpmbData.setLastBindTime(data[LockDataType.dLockLastBindTime.getCode()]);
        rpmbData.setUnlockDate(data[LockDataType.dLockUnlockDate.getCode()]);
        String lockData = buildVaildLockDataString(rpmbData.toString());
        if (lockData == null || 128 != lockData.length()) {
            return null;
        }
        return lockData;
    }

    protected static synchronized boolean updateLockDataToRpmb(String value) {
        synchronized (THRpmbUtil.class) {
            if (TextUtils.isEmpty(value)) {
                return false;
            }
            if (DBG) {
                Rlog.d(TAG, "updataLockDataToRpmb value = " + value.toString());
            }
            MethodBuffer methodBuffer = new MethodBuffer(MethodType.writeSimLockDataFromRPMB);
            methodBuffer.appendStringParam(MethodParamType.backSimLockData, value);
            try {
                if (isMethodExecuteSuccess(RpmbResultParser.parse(RpmbChannel.getInstance().processCmdV2(methodBuffer.buildBuffer())), MethodType.writeSimLockDataFromRPMB)) {
                    Rlog.d(TAG, "updateLockDataToRpmb success");
                    return true;
                }
            } catch (Exception e) {
            }
            return false;
        }
    }

    private static String buildVaildLockDataString(String value) {
        if (!TextUtils.isEmpty(value)) {
            for (int i = value.length(); i < 128; i++) {
                value = value + "*";
            }
        }
        return value;
    }
}
