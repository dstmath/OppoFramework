package com.oppo.internal.telephony.explock.util;

import android.content.Context;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.oppo.internal.telephony.explock.OemLockUtils;
import com.oppo.internal.telephony.explock.util.RpmbResultParser;
import com.oppo.internal.telephony.utils.OppoEngineerManager;

public final class RpmbUtil {
    private static final boolean DBG = true;
    private static final int DEVICE_LOCK_DATA_SIZE = 120;
    private static final int REGION_LOCK_DATA_SIZE = 8;
    private static final int RPMB_DATA_SIZE = 128;
    private static final String TAG = "RpmbUtil";
    private static final int TYPE_SIZE = 12;
    private static final boolean VDBG = "true".equals(SystemProperties.get("persist.sys.oem.rpmbutil", "false"));
    private static final String initFlag = "5";
    private static final String initValue = "808464432";
    private static boolean sHasClearRpmbValue = false;
    private static boolean sHasMovedDeviceDataToRpmb = false;
    private static boolean sHasMovedRegionDataToRpmb = false;

    private RpmbUtil() {
    }

    public static boolean hasMovedDataToRpmb() {
        if (OemLockUtils.isLockUpgradeProject()) {
            return THRpmbUtil.hasMovedDataToRpmb();
        }
        if (VDBG) {
            Rlog.d(TAG, "hasMovedDataToRpmb");
        }
        ValueResult valueResult = hasMovedDataToRpmbValue();
        if (valueResult == null || !((Boolean) valueResult.getRegionValue()).booleanValue()) {
            return false;
        }
        return DBG;
    }

    private static ValueResult hasMovedDataToRpmbValue() {
        if (OemLockUtils.isLockUpgradeProject()) {
            return THRpmbUtil.hasMovedDataToRpmbValue();
        }
        if (VDBG) {
            Rlog.d(TAG, "hasMovedDataToRpmbValue");
        }
        ValueResult valueResult = new ValueResult();
        if (sHasMovedRegionDataToRpmb) {
            if (VDBG) {
                Rlog.d(TAG, "hasMovedDataToRpmbValue sHasMovedDataToRpmb is true");
            }
            valueResult.setGetValueSuccess(DBG);
            valueResult.setDeviceValue(Boolean.valueOf((boolean) DBG));
            valueResult.setRegionValue(Boolean.valueOf((boolean) DBG));
            return valueResult;
        }
        MethodBuffer methodBuffer = new MethodBuffer(MethodType.readSimLockDataFromRPMB);
        methodBuffer.appendStringParam(MethodParamType.backSimLockData, MethodParamType.backSimLockData.name());
        try {
            RpmbResultParser.ResultSummary resultSummary = RpmbResultParser.parse(RpmbChannel.getInstance().processCmdV2(methodBuffer.buildBuffer()));
            if (isMethodExecuteSuccess(resultSummary, MethodType.readSimLockDataFromRPMB)) {
                valueResult.setGetValueSuccess(DBG);
                RpmbResultParser.ResultParam resultParam = resultSummary.getResultParam(MethodParamType.backSimLockData);
                if (resultParam != null) {
                    byte[] resultParamBuffer = resultParam.getBuffer();
                    if (!Util.isArrayEmpty(resultParamBuffer)) {
                        String result = String.valueOf(Util.byteArrayToInt(resultParamBuffer));
                        if (initValue.equals(result)) {
                            sHasClearRpmbValue = DBG;
                        } else {
                            sHasClearRpmbValue = false;
                        }
                        String readValue = Util.encodeWithUtf8(resultParamBuffer);
                        String[] parseResult = parseRpmbDataValues(readValue);
                        if (parseResult == null || parseResult.length != 12) {
                            sHasMovedDeviceDataToRpmb = false;
                            sHasMovedRegionDataToRpmb = false;
                        } else {
                            String operator = parseResult[0];
                            String country = parseResult[9];
                            if (!TextUtils.isEmpty(operator) && OemLockUtils.OPERATOR_LIST.contains(operator)) {
                                sHasMovedDeviceDataToRpmb = DBG;
                                sHasMovedRegionDataToRpmb = DBG;
                            } else if (!TextUtils.isEmpty(country) && !"NA".equals(country)) {
                                sHasMovedDeviceDataToRpmb = false;
                                sHasMovedRegionDataToRpmb = DBG;
                            }
                        }
                        if (VDBG) {
                            Rlog.d(TAG, "hasMovedDataToRpmbValue result = " + result + "\nreadValue = " + readValue + "\nsHasMovedDeviceDataToRpmb = " + sHasMovedDeviceDataToRpmb + ",sHasMovedRegionDataToRpmb = " + sHasMovedRegionDataToRpmb);
                        }
                        valueResult.setDeviceValue(Boolean.valueOf(sHasMovedDeviceDataToRpmb));
                        valueResult.setRegionValue(Boolean.valueOf(sHasMovedRegionDataToRpmb));
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

    public static synchronized boolean moveDataToRpmb(boolean isLockEnabled, boolean forceUpdate) {
        synchronized (RpmbUtil.class) {
            if (OemLockUtils.isLockUpgradeProject()) {
                return THRpmbUtil.moveDataToRpmb(isLockEnabled, forceUpdate);
            }
            if (VDBG) {
                Rlog.d(TAG, "moveDataToRpmb forceUpdate = " + forceUpdate);
            }
            ValueResult hasMovedDataToRpmbValue = hasMovedDataToRpmbValue();
            if (hasMovedDataToRpmbValue == null || !hasMovedDataToRpmbValue.isGetValueSuccess()) {
                Rlog.w(TAG, "moveDataToRpmb hasMovedDataToRpmbValue = " + hasMovedDataToRpmbValue);
                return false;
            }
            Object deviceValue = hasMovedDataToRpmbValue.getDeviceValue();
            Object regionValue = hasMovedDataToRpmbValue.getDeviceValue();
            if (!(deviceValue instanceof Boolean) || !(regionValue instanceof Boolean) || (((Boolean) deviceValue).booleanValue() && ((Boolean) regionValue).booleanValue() && !forceUpdate)) {
                Rlog.e(TAG, "moveDataToRpmb hasMovedDataToRpmb is true");
            } else {
                String cardflag = OppoEngineerManager.getSingleDoubleCardStatus();
                if (!sHasClearRpmbValue || !initFlag.equals(cardflag)) {
                    DeviceLockData dData = OemLockUtils.getDeviceLockDataToRpmb(DBG);
                    RegionLockData rData = OemLockUtils.getRegionNetLockDataToRpmb();
                    if (dData == null || rData == null) {
                        Rlog.e(TAG, "moveDataToRpmb dData is null");
                        return false;
                    }
                    String dDataStr = dData.toString();
                    String rDataStr = rData.toString();
                    if (TextUtils.isEmpty(dDataStr) || TextUtils.isEmpty(rDataStr)) {
                        Rlog.e(TAG, "moveDataToRpmb dDataStr or rDataStr is empty");
                        return false;
                    }
                    MethodBuffer methodBuffer = new MethodBuffer(MethodType.writeSimLockDataFromRPMB);
                    if (VDBG) {
                        Rlog.d(TAG, "moveDataToRpmb dDataStr = " + dDataStr.toString());
                        Rlog.d(TAG, "moveDataToRpmb rDataStr = " + rDataStr.toString());
                    }
                    String dDataStr2 = buildVaildDeviceLockDataString(dDataStr);
                    if (VDBG) {
                        Rlog.d(TAG, "moveDataToRpmb dDataStr = " + dDataStr2.toString() + "length " + dDataStr2.length());
                    }
                    String rDataStr2 = buildVaildRegionLockDataString(rDataStr);
                    if (VDBG) {
                        Rlog.d(TAG, "moveDataToRpmb rDataStr = " + rDataStr2.toString() + "length " + rDataStr2.length());
                    }
                    String dataStr = dDataStr2 + rDataStr2;
                    if (TextUtils.isEmpty(dataStr)) {
                        Rlog.e(TAG, "moveDataToRpmb dataStr is null");
                        return false;
                    }
                    methodBuffer.appendStringParam(MethodParamType.backSimLockData, dataStr);
                    try {
                        if (isMethodExecuteSuccess(RpmbResultParser.parse(RpmbChannel.getInstance().processCmdV2(methodBuffer.buildBuffer())), MethodType.writeSimLockDataFromRPMB)) {
                            return DBG;
                        }
                        Rlog.e(TAG, "moveDataToRpmb isMethodExecuteSuccess is false");
                    } catch (Exception e) {
                        Rlog.e(TAG, "moveDataToRpmb e = " + e);
                    }
                } else {
                    Rlog.e(TAG, "moveDataToRpmb need to do clear device");
                    return false;
                }
            }
            return false;
        }
    }

    public static void checkMoveDataToRpmb() {
        if (OemLockUtils.isLockUpgradeProject()) {
            THRpmbUtil.checkMoveDataToRpmb();
        }
        if (hasMovedDataToRpmb()) {
            return;
        }
        if (moveDataToRpmb(DBG, false)) {
            Rlog.d(TAG, "checkMoveDataToRpmb moveDataToRpmb success!");
            sHasMovedDeviceDataToRpmb = DBG;
            sHasMovedRegionDataToRpmb = DBG;
            return;
        }
        Rlog.e(TAG, "checkMoveDataToRpmb moveDataToRpmb failed!");
    }

    private static String getResultParamValue(RpmbResultParser.ResultParam resultParam, boolean isResultInt) {
        if (OemLockUtils.isLockUpgradeProject()) {
            return THRpmbUtil.getResultParamValue(resultParam, isResultInt);
        }
        if (resultParam != null) {
            byte[] resultParamBuffer = resultParam.getBuffer();
            if (Util.isArrayEmpty(resultParamBuffer)) {
                Rlog.e(TAG, "getResultParamValue resultParamBuffer is empty");
                return null;
            } else if (isResultInt) {
                return String.valueOf(Util.byteArrayToInt(resultParamBuffer));
            } else {
                return Util.encodeWithUtf8(resultParamBuffer);
            }
        } else {
            Rlog.e(TAG, "getResultParamValue resultParam is null");
            return null;
        }
    }

    private static boolean isMethodExecuteSuccess(RpmbResultParser.ResultSummary resultSummary, MethodType methodType) {
        if (OemLockUtils.isLockUpgradeProject()) {
            return THRpmbUtil.isMethodExecuteSuccess(resultSummary, methodType);
        }
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
                return DBG;
            } else {
                Rlog.e(TAG, "isMethodExecuteSuccess isExeSuccess = false");
                return false;
            }
        }
    }

    private static String[] parseRpmbDataValues(String rpmbValue) {
        String[] lockData;
        if (OemLockUtils.isLockUpgradeProject()) {
            return THRpmbUtil.parseRpmbDataValues(rpmbValue);
        }
        String[] data = new String[12];
        if (TextUtils.isEmpty(rpmbValue) || rpmbValue.length() != 128 || (lockData = rpmbValue.split("\\$")) == null || lockData.length != 12) {
            return null;
        }
        for (int i = 0; i < 12; i++) {
            data[i] = lockData[i];
        }
        return data;
    }

    public static synchronized boolean initDefaultDataToRpmb(Context context) {
        synchronized (RpmbUtil.class) {
            if (OemLockUtils.isLockUpgradeProject()) {
                return THRpmbUtil.initDefaultDataToRpmb(context);
            }
            if (VDBG) {
                Rlog.d(TAG, "initDefaultDataToRpmb");
            }
            String defaultData = "00000000";
            for (int i = defaultData.length(); i < 128; i++) {
                defaultData = defaultData + "0";
            }
            if (VDBG) {
                Rlog.d(TAG, "initDefaultDataToRpmb defaultData = " + defaultData.toString() + "length = " + defaultData.length());
            }
            MethodBuffer methodBuffer = new MethodBuffer(MethodType.writeSimLockDataFromRPMB);
            methodBuffer.appendStringParam(MethodParamType.backSimLockData, defaultData);
            try {
                if (isMethodExecuteSuccess(RpmbResultParser.parse(RpmbChannel.getInstance().processCmdV2(methodBuffer.buildBuffer())), MethodType.writeSimLockDataFromRPMB)) {
                    Rlog.d(TAG, "initDefaultDataToRpmb success");
                    return DBG;
                }
                Rlog.e(TAG, "initDefaultDataToRpmb isMethodExecuteSuccess is false");
                return false;
            } catch (Exception e) {
                Rlog.e(TAG, "initDefaultDataToRpmb e = " + e);
            }
        }
    }

    private static synchronized String getRpmbLockDataString() {
        synchronized (RpmbUtil.class) {
            if (OemLockUtils.isLockUpgradeProject()) {
                return THRpmbUtil.getRpmbLockDataString();
            }
            ValueResult valueResult = new ValueResult();
            MethodBuffer methodBuffer = new MethodBuffer(MethodType.readSimLockDataFromRPMB);
            methodBuffer.appendStringParam(MethodParamType.backSimLockData, MethodParamType.backSimLockData.name());
            try {
                RpmbResultParser.ResultSummary resultSummary = RpmbResultParser.parse(RpmbChannel.getInstance().processCmdV2(methodBuffer.buildBuffer()));
                if (isMethodExecuteSuccess(resultSummary, MethodType.readSimLockDataFromRPMB)) {
                    valueResult.setGetValueSuccess(DBG);
                    RpmbResultParser.ResultParam resultParam = resultSummary.getResultParam(MethodParamType.backSimLockData);
                    if (resultParam != null) {
                        byte[] resultParamBuffer = resultParam.getBuffer();
                        if (!Util.isArrayEmpty(resultParamBuffer)) {
                            String.valueOf(Util.byteArrayToInt(resultParamBuffer));
                            String readValue = Util.encodeWithUtf8(resultParamBuffer);
                            String[] parseResult = parseRpmbDataValues(readValue);
                            if (parseResult != null && parseResult.length == 12) {
                                if (VDBG) {
                                    Rlog.e(TAG, "getRpmbLockDataString result readValue = " + readValue);
                                }
                                return readValue;
                            }
                        } else {
                            Rlog.e(TAG, "getRpmbLockDataString resultParamBuffer is empty");
                        }
                    } else {
                        Rlog.e(TAG, "getRpmbLockDataString resultParam is null");
                    }
                }
            } catch (Exception e) {
                Rlog.e(TAG, "getRpmbLockDataString e = " + e);
            }
            return null;
        }
    }

    public static String getRpmbOperatorData() {
        if (OemLockUtils.isLockUpgradeProject()) {
            return THRpmbUtil.getRpmbOperatorData();
        }
        String[] parseResult = parseRpmbDataValues(getRpmbLockDataString());
        if (parseResult == null || parseResult.length != 12 || !OemLockUtils.OPERATOR_LIST.contains(parseResult[LockDataType.dLockOperator.getCode()])) {
            return null;
        }
        return parseResult[LockDataType.dLockOperator.getCode()];
    }

    public static String getRpmbLockStatus() {
        if (OemLockUtils.isLockUpgradeProject()) {
            return THRpmbUtil.getRpmbLockStatus();
        }
        String[] parseResult = parseRpmbDataValues(getRpmbLockDataString());
        if (parseResult == null || parseResult.length != 12) {
            return null;
        }
        return parseResult[LockDataType.dLockStatus.getCode()];
    }

    public static boolean setRpmbLockStatus(String status) {
        String[] updateParseResult;
        if (OemLockUtils.isLockUpgradeProject()) {
            return THRpmbUtil.setRpmbLockStatus(status);
        }
        String[] parseResult = parseRpmbDataValues(getRpmbLockDataString());
        if (parseResult != null && parseResult.length == 12) {
            parseResult[LockDataType.dLockStatus.getCode()] = status;
        }
        if (!updateLockDataToRpmb(buildStringLockDataToRpmb(parseResult)) || (updateParseResult = parseRpmbDataValues(getRpmbLockDataString())) == null || updateParseResult.length != 12 || !status.equals(updateParseResult[LockDataType.dLockStatus.getCode()])) {
            return false;
        }
        return DBG;
    }

    public static String getRpmbLockIMSI() {
        if (OemLockUtils.isLockUpgradeProject()) {
            return THRpmbUtil.getRpmbLockIMSI();
        }
        String[] parseResult = parseRpmbDataValues(getRpmbLockDataString());
        if (parseResult == null || parseResult.length != 12) {
            return null;
        }
        return parseResult[LockDataType.dLockImsi.getCode()];
    }

    public static boolean setRpmbLockIMSI(String imsi) {
        String[] updateParseResult;
        if (OemLockUtils.isLockUpgradeProject()) {
            return THRpmbUtil.setRpmbLockIMSI(imsi);
        }
        String[] parseResult = parseRpmbDataValues(getRpmbLockDataString());
        if (parseResult != null && parseResult.length == 12) {
            parseResult[LockDataType.dLockImsi.getCode()] = imsi;
        }
        if (!updateLockDataToRpmb(buildStringLockDataToRpmb(parseResult)) || (updateParseResult = parseRpmbDataValues(getRpmbLockDataString())) == null || updateParseResult.length != 12 || !imsi.equals(updateParseResult[LockDataType.dLockImsi.getCode()])) {
            return false;
        }
        return DBG;
    }

    public static String getRpmbLockIccid() {
        if (OemLockUtils.isLockUpgradeProject()) {
            return THRpmbUtil.getRpmbLockIccid();
        }
        String[] parseResult = parseRpmbDataValues(getRpmbLockDataString());
        if (parseResult == null || parseResult.length != 12) {
            return null;
        }
        return parseResult[LockDataType.dLockIccid.getCode()];
    }

    public static boolean setRpmbLockIccid(String iccid) {
        String[] updateParseResult;
        if (OemLockUtils.isLockUpgradeProject()) {
            return THRpmbUtil.setRpmbLockIccid(iccid);
        }
        String[] parseResult = parseRpmbDataValues(getRpmbLockDataString());
        if (parseResult != null && parseResult.length == 12) {
            parseResult[LockDataType.dLockIccid.getCode()] = iccid;
        }
        if (!updateLockDataToRpmb(buildStringLockDataToRpmb(parseResult)) || (updateParseResult = parseRpmbDataValues(getRpmbLockDataString())) == null || updateParseResult.length != 12 || !iccid.equals(updateParseResult[LockDataType.dLockIccid.getCode()])) {
            return false;
        }
        return DBG;
    }

    public static String getRpmbLockDays() {
        if (OemLockUtils.isLockUpgradeProject()) {
            return THRpmbUtil.getRpmbLockDays();
        }
        String[] parseResult = parseRpmbDataValues(getRpmbLockDataString());
        if (parseResult == null || parseResult.length != 12) {
            return null;
        }
        return parseResult[LockDataType.dLockContractDays.getCode()];
    }

    public static boolean setRpmbLockDays(String days) {
        String[] updateParseResult;
        if (OemLockUtils.isLockUpgradeProject()) {
            return THRpmbUtil.setRpmbLockDays(days);
        }
        String[] parseResult = parseRpmbDataValues(getRpmbLockDataString());
        if (parseResult != null && parseResult.length == 12) {
            parseResult[LockDataType.dLockContractDays.getCode()] = days;
        }
        if (!updateLockDataToRpmb(buildStringLockDataToRpmb(parseResult)) || (updateParseResult = parseRpmbDataValues(getRpmbLockDataString())) == null || updateParseResult.length != 12 || !days.equals(updateParseResult[LockDataType.dLockContractDays.getCode()])) {
            return false;
        }
        return DBG;
    }

    public static String getRpmbLockFirstBindTime() {
        if (OemLockUtils.isLockUpgradeProject()) {
            return THRpmbUtil.getRpmbLockFirstBindTime();
        }
        String[] parseResult = parseRpmbDataValues(getRpmbLockDataString());
        if (parseResult == null || parseResult.length != 12 || !OemLockUtils.OPERATOR_LIST.contains(parseResult[LockDataType.dLockOperator.getCode()])) {
            return null;
        }
        return parseResult[LockDataType.dLockFirstBindTime.getCode()];
    }

    public static boolean setRpmbLockFirstBindTime(String times) {
        String[] updateParseResult;
        if (OemLockUtils.isLockUpgradeProject()) {
            return THRpmbUtil.setRpmbLockFirstBindTime(times);
        }
        String[] parseResult = parseRpmbDataValues(getRpmbLockDataString());
        if (parseResult != null && parseResult.length == 12) {
            parseResult[LockDataType.dLockFirstBindTime.getCode()] = times;
        }
        if (!updateLockDataToRpmb(buildStringLockDataToRpmb(parseResult)) || (updateParseResult = parseRpmbDataValues(getRpmbLockDataString())) == null || updateParseResult.length != 12 || !times.equals(updateParseResult[LockDataType.dLockFirstBindTime.getCode()])) {
            return false;
        }
        return DBG;
    }

    public static String getRpmbLockLastBindTime() {
        if (OemLockUtils.isLockUpgradeProject()) {
            return THRpmbUtil.getRpmbLockLastBindTime();
        }
        String[] parseResult = parseRpmbDataValues(getRpmbLockDataString());
        if (parseResult == null || parseResult.length != 12) {
            return null;
        }
        return parseResult[LockDataType.dLockLastBindTime.getCode()];
    }

    public static boolean setRpmbLockLastBindTime(String times) {
        String[] updateParseResult;
        if (OemLockUtils.isLockUpgradeProject()) {
            return THRpmbUtil.setRpmbLockLastBindTime(times);
        }
        String[] parseResult = parseRpmbDataValues(getRpmbLockDataString());
        if (parseResult != null && parseResult.length == 12) {
            parseResult[LockDataType.dLockLastBindTime.getCode()] = times;
        }
        if (!updateLockDataToRpmb(buildStringLockDataToRpmb(parseResult)) || (updateParseResult = parseRpmbDataValues(getRpmbLockDataString())) == null || updateParseResult.length != 12 || !times.equals(updateParseResult[LockDataType.dLockLastBindTime.getCode()])) {
            return false;
        }
        return DBG;
    }

    public static String getRpmbLockUnlockTime() {
        if (OemLockUtils.isLockUpgradeProject()) {
            return THRpmbUtil.getRpmbLockUnlockTime();
        }
        String[] parseResult = parseRpmbDataValues(getRpmbLockDataString());
        if (parseResult == null || parseResult.length != 12) {
            return null;
        }
        return parseResult[LockDataType.dLockUnlockDate.getCode()];
    }

    public static boolean setRpmbLockUnlockTime(String time) {
        String[] updateParseResult;
        if (OemLockUtils.isLockUpgradeProject()) {
            return THRpmbUtil.setRpmbLockUnlockTime(time);
        }
        String[] parseResult = parseRpmbDataValues(getRpmbLockDataString());
        if (parseResult != null && parseResult.length == 12) {
            parseResult[LockDataType.dLockUnlockDate.getCode()] = time;
        }
        if (!updateLockDataToRpmb(buildStringLockDataToRpmb(parseResult)) || (updateParseResult = parseRpmbDataValues(getRpmbLockDataString())) == null || updateParseResult.length != 12 || !time.equals(updateParseResult[LockDataType.dLockUnlockDate.getCode()])) {
            return false;
        }
        return DBG;
    }

    public static String buildStringLockDataToRpmb(String[] data) {
        if (OemLockUtils.isLockUpgradeProject()) {
            return THRpmbUtil.buildStringLockDataToRpmb(data);
        }
        if (data == null || data.length != 12) {
            return null;
        }
        DeviceLockData dData = new DeviceLockData();
        dData.setLockedOperator(data[LockDataType.dLockOperator.getCode()]);
        dData.setLockedState(data[LockDataType.dLockStatus.getCode()]);
        dData.setLockedIMSI(data[LockDataType.dLockImsi.getCode()]);
        dData.setContractDays(data[LockDataType.dLockContractDays.getCode()]);
        dData.setFirstBindTime(data[LockDataType.dLockFirstBindTime.getCode()]);
        dData.setLockedICCID(data[LockDataType.dLockIccid.getCode()]);
        dData.setLastBindTime(data[LockDataType.dLockLastBindTime.getCode()]);
        dData.setUnlockDate(data[LockDataType.dLockUnlockDate.getCode()]);
        String lockDeviceDataStr = buildVaildDeviceLockDataString(dData.toString());
        RegionLockData rData = new RegionLockData();
        rData.setRegionLockCountry(data[LockDataType.rLockCountry.getCode()]);
        rData.setRegionLockStatus(data[LockDataType.rLockStatus.getCode()]);
        String lockRegionDataStr = buildVaildRegionLockDataString(rData.toString());
        String lockData = lockDeviceDataStr + lockRegionDataStr;
        if (VDBG) {
            Rlog.d(TAG, "buildStringLockDataToRpmb lockDeviceDataStr = " + lockDeviceDataStr + ", lockRegionDataStr = " + lockRegionDataStr);
        }
        if (lockData == null || 128 != lockData.length()) {
            return null;
        }
        return lockData;
    }

    private static synchronized boolean updateLockDataToRpmb(String value) {
        synchronized (RpmbUtil.class) {
            if (OemLockUtils.isLockUpgradeProject()) {
                return THRpmbUtil.updateLockDataToRpmb(value);
            } else if (TextUtils.isEmpty(value)) {
                return false;
            } else {
                if (VDBG) {
                    Rlog.d(TAG, "updataLockDataToRpmb value = " + value.toString());
                }
                MethodBuffer methodBuffer = new MethodBuffer(MethodType.writeSimLockDataFromRPMB);
                methodBuffer.appendStringParam(MethodParamType.backSimLockData, value);
                try {
                    if (isMethodExecuteSuccess(RpmbResultParser.parse(RpmbChannel.getInstance().processCmdV2(methodBuffer.buildBuffer())), MethodType.writeSimLockDataFromRPMB)) {
                        Rlog.d(TAG, "updateLockDataToRpmb success");
                        return DBG;
                    }
                } catch (Exception e) {
                    Rlog.e(TAG, "updateLockDataToRpmb e = " + e);
                }
                return false;
            }
        }
    }

    private static String buildVaildDeviceLockDataString(String value) {
        if (!TextUtils.isEmpty(value)) {
            for (int i = value.length(); i < 120; i++) {
                value = i == 119 ? value + "$" : value + "*";
            }
        }
        return value;
    }

    private static String buildVaildRegionLockDataString(String value) {
        if (!TextUtils.isEmpty(value)) {
            for (int i = value.length(); i < 8; i++) {
                value = value + "*";
            }
        }
        return value;
    }

    public static String getRpmbRegionLockCountry() {
        String[] parseResult = parseRpmbDataValues(getRpmbLockDataString());
        if (parseResult == null || parseResult.length != 12) {
            return "NA";
        }
        return parseResult[LockDataType.rLockCountry.getCode()];
    }

    public static boolean setRpmbRegionLockCountry(String country) {
        String[] updateParseResult;
        String[] parseResult = parseRpmbDataValues(getRpmbLockDataString());
        if (parseResult != null && parseResult.length == 12) {
            parseResult[LockDataType.rLockCountry.getCode()] = country;
        }
        if (!updateLockDataToRpmb(buildStringLockDataToRpmb(parseResult)) || (updateParseResult = parseRpmbDataValues(getRpmbLockDataString())) == null || updateParseResult.length != 12) {
            return false;
        }
        if (country.equals(updateParseResult[LockDataType.rLockCountry.getCode()])) {
            SystemProperties.set(OemLockUtils.REGION_NETLOCK_RPMB, country);
            return DBG;
        }
        SystemProperties.set(OemLockUtils.REGION_NETLOCK_RPMB, "NA");
        return false;
    }

    public static String getRpmbRegionLockStatus() {
        String[] parseResult = parseRpmbDataValues(getRpmbLockDataString());
        if (parseResult == null || parseResult.length != 12) {
            return null;
        }
        return parseResult[LockDataType.rLockStatus.getCode()];
    }

    public static boolean setRpmbRegionLockStatus(String status) {
        String[] updateParseResult;
        String[] parseResult = parseRpmbDataValues(getRpmbLockDataString());
        if (parseResult != null && parseResult.length == 12) {
            parseResult[LockDataType.rLockStatus.getCode()] = status;
        }
        if (!updateLockDataToRpmb(buildStringLockDataToRpmb(parseResult)) || (updateParseResult = parseRpmbDataValues(getRpmbLockDataString())) == null || updateParseResult.length != 12 || !status.equals(updateParseResult[LockDataType.rLockStatus.getCode()])) {
            return false;
        }
        return DBG;
    }
}
