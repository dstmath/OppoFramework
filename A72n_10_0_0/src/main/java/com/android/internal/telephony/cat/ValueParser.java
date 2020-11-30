package com.android.internal.telephony.cat;

import android.annotation.UnsupportedAppUsage;
import android.content.res.Resources;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.cat.Duration;
import com.android.internal.telephony.uicc.IccUtils;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public abstract class ValueParser {
    public static CommandDetails retrieveCommandDetails(ComprehensionTlv ctlv) throws ResultException {
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

    public static DeviceIdentities retrieveDeviceIdentities(ComprehensionTlv ctlv) throws ResultException {
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

    public static Duration retrieveDuration(ComprehensionTlv ctlv) throws ResultException {
        Duration.TimeUnit timeUnit = Duration.TimeUnit.SECOND;
        byte[] rawValue = ctlv.getRawValue();
        int valueIndex = ctlv.getValueIndex();
        try {
            return new Duration(rawValue[valueIndex + 1] & 255, Duration.TimeUnit.values()[rawValue[valueIndex] & 255]);
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

    public static int retrieveItemId(ComprehensionTlv ctlv) throws ResultException {
        try {
            return ctlv.getRawValue()[ctlv.getValueIndex()] & 255;
        } catch (IndexOutOfBoundsException e) {
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
        }
    }

    public static IconId retrieveIconId(ComprehensionTlv ctlv) throws ResultException {
        IconId id = new IconId();
        byte[] rawValue = ctlv.getRawValue();
        int valueIndex = ctlv.getValueIndex();
        int valueIndex2 = valueIndex + 1;
        try {
            id.selfExplanatory = (rawValue[valueIndex] & 255) == 0;
            id.recordNumber = rawValue[valueIndex2] & 255;
            return id;
        } catch (IndexOutOfBoundsException e) {
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r1v1, resolved type: byte[] */
    /* JADX DEBUG: Multi-variable search result rejected for r2v2, resolved type: byte */
    /* JADX DEBUG: Multi-variable search result rejected for r4v3, resolved type: int[] */
    /* JADX DEBUG: Multi-variable search result rejected for r5v4, resolved type: byte */
    /* JADX WARN: Multi-variable type inference failed */
    public static ItemsIconId retrieveItemsIconId(ComprehensionTlv ctlv) throws ResultException {
        CatLog.d("ValueParser", "retrieveItemsIconId:");
        ItemsIconId id = new ItemsIconId();
        byte[] rawValue = ctlv.getRawValue();
        int valueIndex = ctlv.getValueIndex();
        boolean z = true;
        int numOfItems = ctlv.getLength() - 1;
        id.recordNumbers = new int[numOfItems];
        int valueIndex2 = valueIndex + 1;
        try {
            if ((rawValue[valueIndex] & 255) != 0) {
                z = false;
            }
            id.selfExplanatory = z;
            int index = 0;
            while (index < numOfItems) {
                int index2 = index + 1;
                int valueIndex3 = valueIndex2 + 1;
                try {
                    id.recordNumbers[index] = rawValue[valueIndex2];
                    index = index2;
                    valueIndex2 = valueIndex3;
                } catch (IndexOutOfBoundsException e) {
                    throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
                }
            }
            return id;
        } catch (IndexOutOfBoundsException e2) {
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
        }
    }

    @UnsupportedAppUsage
    public static List<TextAttribute> retrieveTextAttribute(ComprehensionTlv ctlv) throws ResultException {
        FontSize size;
        ArrayList<TextAttribute> lst = new ArrayList<>();
        byte[] rawValue = ctlv.getRawValue();
        int valueIndex = ctlv.getValueIndex();
        int length = ctlv.getLength();
        if (length == 0) {
            return null;
        }
        int itemCount = length / 4;
        int i = 0;
        int valueIndex2 = valueIndex;
        while (i < itemCount) {
            try {
                int start = rawValue[valueIndex2] & 255;
                int textLength = rawValue[valueIndex2 + 1] & 255;
                int format = rawValue[valueIndex2 + 2] & 255;
                int colorValue = rawValue[valueIndex2 + 3] & 255;
                TextAlignment align = TextAlignment.fromInt(format & 3);
                FontSize size2 = FontSize.fromInt((format >> 2) & 3);
                if (size2 == null) {
                    size = FontSize.NORMAL;
                } else {
                    size = size2;
                }
                boolean strikeThrough = false;
                boolean bold = (format & 16) != 0;
                boolean italic = (format & 32) != 0;
                boolean underlined = (format & 64) != 0;
                if ((format & 128) != 0) {
                    strikeThrough = true;
                }
                lst.add(new TextAttribute(start, textLength, align, size, bold, italic, underlined, strikeThrough, TextColor.fromInt(colorValue)));
                i++;
                valueIndex2 += 4;
            } catch (IndexOutOfBoundsException e) {
                throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
            }
        }
        return lst;
    }

    @UnsupportedAppUsage
    public static String retrieveAlphaId(ComprehensionTlv ctlv) throws ResultException {
        boolean noAlphaUsrCnf;
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
            } else {
                CatLog.d("ValueParser", "Alpha Id length=" + length);
                return null;
            }
        } else {
            try {
                noAlphaUsrCnf = Resources.getSystem().getBoolean(17891527);
            } catch (Resources.NotFoundException e2) {
                noAlphaUsrCnf = false;
            }
            if (noAlphaUsrCnf) {
                return null;
            }
            return CatService.STK_DEFAULT;
        }
    }

    @UnsupportedAppUsage
    public static String retrieveTextString(ComprehensionTlv ctlv) throws ResultException {
        byte[] rawValue = ctlv.getRawValue();
        int valueIndex = ctlv.getValueIndex();
        int textLen = ctlv.getLength();
        if (textLen == 0) {
            return null;
        }
        int textLen2 = textLen - 1;
        try {
            byte codingScheme = (byte) (rawValue[valueIndex] & 12);
            if (codingScheme == 0) {
                return GsmAlphabet.gsm7BitPackedToString(rawValue, valueIndex + 1, (textLen2 * 8) / 7);
            }
            if (codingScheme == 4) {
                return GsmAlphabet.gsm8BitUnpackedToString(rawValue, valueIndex + 1, textLen2);
            }
            if (codingScheme == 8) {
                return new String(rawValue, valueIndex + 1, textLen2, "UTF-16");
            }
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
        } catch (IndexOutOfBoundsException e) {
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
        } catch (UnsupportedEncodingException e2) {
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
        }
    }
}
