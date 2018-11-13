package com.android.internal.telephony.cat;

import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.cat.Duration.TimeUnit;
import com.android.internal.telephony.uicc.IccUtils;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

abstract class ValueParser {
    ValueParser() {
    }

    static CommandDetails retrieveCommandDetails(ComprehensionTlv ctlv) throws ResultException {
        CommandDetails cmdDet = new CommandDetails();
        byte[] rawValue = ctlv.getRawValue();
        int valueIndex = ctlv.getValueIndex();
        try {
            cmdDet.compRequired = ctlv.isComprehensionRequired();
            cmdDet.commandNumber = rawValue[valueIndex] & 255;
            cmdDet.typeOfCommand = rawValue[valueIndex + 1] & 255;
            cmdDet.commandQualifier = rawValue[valueIndex + 2] & 255;
            return cmdDet;
        } catch (IndexOutOfBoundsException e) {
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
        }
    }

    static DeviceIdentities retrieveDeviceIdentities(ComprehensionTlv ctlv) throws ResultException {
        DeviceIdentities devIds = new DeviceIdentities();
        byte[] rawValue = ctlv.getRawValue();
        int valueIndex = ctlv.getValueIndex();
        try {
            devIds.sourceId = rawValue[valueIndex] & 255;
            devIds.destinationId = rawValue[valueIndex + 1] & 255;
            return devIds;
        } catch (IndexOutOfBoundsException e) {
            throw new ResultException(ResultCode.REQUIRED_VALUES_MISSING);
        }
    }

    static Duration retrieveDuration(ComprehensionTlv ctlv) throws ResultException {
        TimeUnit timeUnit = TimeUnit.SECOND;
        byte[] rawValue = ctlv.getRawValue();
        int valueIndex = ctlv.getValueIndex();
        try {
            return new Duration(rawValue[valueIndex + 1] & 255, TimeUnit.values()[rawValue[valueIndex] & 255]);
        } catch (IndexOutOfBoundsException e) {
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
        }
    }

    static Item retrieveItem(ComprehensionTlv ctlv) throws ResultException {
        byte[] rawValue = ctlv.getRawValue();
        int valueIndex = ctlv.getValueIndex();
        int length = ctlv.getLength();
        if (length == 0) {
            return null;
        }
        try {
            return new Item(rawValue[valueIndex] & 255, IccUtils.adnStringFieldToString(rawValue, valueIndex + 1, length - 1));
        } catch (IndexOutOfBoundsException e) {
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
        }
    }

    static int retrieveItemId(ComprehensionTlv ctlv) throws ResultException {
        try {
            return ctlv.getRawValue()[ctlv.getValueIndex()] & 255;
        } catch (IndexOutOfBoundsException e) {
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
        }
    }

    static IconId retrieveIconId(ComprehensionTlv ctlv) throws ResultException {
        boolean z = false;
        IconId id = new IconId();
        byte[] rawValue = ctlv.getRawValue();
        int valueIndex = ctlv.getValueIndex();
        int valueIndex2 = valueIndex + 1;
        try {
            if ((rawValue[valueIndex] & 255) == 0) {
                z = true;
            }
            id.selfExplanatory = z;
            id.recordNumber = rawValue[valueIndex2] & 255;
            return id;
        } catch (IndexOutOfBoundsException e) {
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
        }
    }

    static ItemsIconId retrieveItemsIconId(ComprehensionTlv ctlv) throws ResultException {
        boolean z = false;
        CatLog.d("ValueParser", "retrieveItemsIconId:");
        ItemsIconId id = new ItemsIconId();
        byte[] rawValue = ctlv.getRawValue();
        int valueIndex = ctlv.getValueIndex();
        int numOfItems = ctlv.getLength() - 1;
        id.recordNumbers = new int[numOfItems];
        int valueIndex2 = valueIndex + 1;
        try {
            if ((rawValue[valueIndex] & 255) == 0) {
                z = true;
            }
            id.selfExplanatory = z;
            int i = 0;
            while (i < numOfItems) {
                int index = i + 1;
                valueIndex = valueIndex2 + 1;
                try {
                    id.recordNumbers[i] = rawValue[valueIndex2];
                    i = index;
                    valueIndex2 = valueIndex;
                } catch (IndexOutOfBoundsException e) {
                }
            }
            return id;
        } catch (IndexOutOfBoundsException e2) {
            valueIndex = valueIndex2;
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
        }
    }

    static List<TextAttribute> retrieveTextAttribute(ComprehensionTlv ctlv) throws ResultException {
        ArrayList<TextAttribute> lst = new ArrayList();
        byte[] rawValue = ctlv.getRawValue();
        int valueIndex = ctlv.getValueIndex();
        int length = ctlv.getLength();
        if (length == 0) {
            return null;
        }
        int itemCount = length / 4;
        int i = 0;
        while (i < itemCount) {
            try {
                int start = rawValue[valueIndex] & 255;
                int textLength = rawValue[valueIndex + 1] & 255;
                int format = rawValue[valueIndex + 2] & 255;
                int colorValue = rawValue[valueIndex + 3] & 255;
                TextAlignment align = TextAlignment.fromInt(format & 3);
                FontSize size = FontSize.fromInt((format >> 2) & 3);
                if (size == null) {
                    size = FontSize.NORMAL;
                }
                lst.add(new TextAttribute(start, textLength, align, size, (format & 16) != 0, (format & 32) != 0, (format & 64) != 0, (format & 128) != 0, TextColor.fromInt(colorValue)));
                i++;
                valueIndex += 4;
            } catch (IndexOutOfBoundsException e) {
                throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
            }
        }
        return lst;
    }

    static String retrieveAlphaId(ComprehensionTlv ctlv) throws ResultException {
        String str = null;
        if (ctlv != null) {
            byte[] rawValue = ctlv.getRawValue();
            int valueIndex = ctlv.getValueIndex();
            int length = ctlv.getLength();
            if (length != 0) {
                try {
                    return IccUtils.adnStringFieldToString(rawValue, valueIndex, length);
                } catch (IndexOutOfBoundsException e) {
                    throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
                }
            }
            CatLog.d("ValueParser", "Alpha Id length=" + length);
            return null;
        }
        boolean noAlphaUsrCnf;
        try {
            noAlphaUsrCnf = Resources.getSystem().getBoolean(17957024);
        } catch (NotFoundException e2) {
            noAlphaUsrCnf = false;
        }
        if (!noAlphaUsrCnf) {
            str = "Default Message";
        }
        return str;
    }

    static String retrieveTextString(ComprehensionTlv ctlv) throws ResultException {
        byte[] rawValue = ctlv.getRawValue();
        int valueIndex = ctlv.getValueIndex();
        int textLen = ctlv.getLength();
        if (textLen == 0) {
            return null;
        }
        textLen--;
        try {
            String text;
            byte codingScheme = (byte) (rawValue[valueIndex] & 12);
            if (codingScheme == (byte) 0) {
                text = GsmAlphabet.gsm7BitPackedToString(rawValue, valueIndex + 1, (textLen * 8) / 7);
            } else if (codingScheme == (byte) 4) {
                text = GsmAlphabet.gsm8BitUnpackedToString(rawValue, valueIndex + 1, textLen);
            } else if (codingScheme == (byte) 8) {
                text = new String(rawValue, valueIndex + 1, textLen, "UTF-16");
            } else {
                throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
            }
            return text;
        } catch (IndexOutOfBoundsException e) {
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
        } catch (UnsupportedEncodingException e2) {
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
        }
    }

    static int retrieveTarget(ComprehensionTlv ctlv) throws ResultException {
        ActivateDescriptor activateDesc = new ActivateDescriptor();
        try {
            activateDesc.target = ctlv.getRawValue()[ctlv.getValueIndex()] & 255;
            return activateDesc.target;
        } catch (IndexOutOfBoundsException e) {
            throw new ResultException(ResultCode.REQUIRED_VALUES_MISSING);
        }
    }
}
