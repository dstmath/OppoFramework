package com.mediatek.android.mms.pdu;

import android.util.Log;
import com.google.android.mms.InvalidHeaderValueException;
import com.google.android.mms.pdu.PduHeaders;
import java.util.ArrayList;

public class MtkPduHeaders extends PduHeaders {
    public static final int DATE_SENT = 201;
    public static final int STATE_SKIP_RETRYING = 137;
    private static final String TAG = "MtkPduHeaders";

    /* access modifiers changed from: protected */
    public void setOctet(int value, int field) throws InvalidHeaderValueException {
        if (field != 134) {
            if (field != 153) {
                if (field != 165) {
                    if (!(field == 167 || field == 169 || field == 171 || field == 177)) {
                        if (field != 180) {
                            if (field != 191) {
                                if (field != 140) {
                                    if (field != 141) {
                                        if (field != 148) {
                                            if (field != 149) {
                                                if (field != 155) {
                                                    if (field != 156) {
                                                        if (field != 162) {
                                                            if (field != 163) {
                                                                switch (field) {
                                                                    case 143:
                                                                        if (value < 128 || value > 130) {
                                                                            throw new InvalidHeaderValueException("Invalid Octet value!");
                                                                        }
                                                                    case 144:
                                                                    case 145:
                                                                        break;
                                                                    case MtkPduPart.P_DATE /* 146 */:
                                                                        if (value <= 196 || value >= 224) {
                                                                            if ((value > 235 && value <= 255) || value < 128 || ((value > 136 && value < 192) || value > 255)) {
                                                                                value = 224;
                                                                                break;
                                                                            }
                                                                        } else {
                                                                            value = 192;
                                                                            break;
                                                                        }
                                                                    default:
                                                                        switch (field) {
                                                                            case 186:
                                                                                if (value < 128 || value > 135) {
                                                                                    throw new InvalidHeaderValueException("Invalid Octet value!");
                                                                                }
                                                                            case 187:
                                                                            case 188:
                                                                                break;
                                                                            default:
                                                                                throw new RuntimeException("Invalid header field!");
                                                                        }
                                                                        break;
                                                                }
                                                            } else if (value < 128 || value > 132) {
                                                                throw new InvalidHeaderValueException("Invalid Octet value!");
                                                            }
                                                        }
                                                    } else if (value < 128 || value > 131) {
                                                        throw new InvalidHeaderValueException("Invalid Octet value!");
                                                    }
                                                } else if (!(128 == value || 129 == value)) {
                                                    throw new InvalidHeaderValueException("Invalid Octet value!");
                                                }
                                            } else if (value < 128 || value > 137) {
                                                throw new InvalidHeaderValueException("Invalid Octet value!");
                                            }
                                        }
                                    } else if (value < 16 || value > 19) {
                                        value = 18;
                                    }
                                } else if (value < 128 || value > 151) {
                                    throw new InvalidHeaderValueException("Invalid Octet value!");
                                }
                            } else if (!(128 == value || 129 == value)) {
                                throw new InvalidHeaderValueException("Invalid Octet value!");
                            }
                        } else if (128 != value) {
                            throw new InvalidHeaderValueException("Invalid Octet value!");
                        }
                    }
                } else if (value > 193 && value < 224) {
                    value = 192;
                } else if (value > 228 && value <= 255) {
                    value = 224;
                } else if (value < 128 || ((value > 128 && value < 192) || value > 255)) {
                    value = 224;
                }
            } else if (value > 194 && value < 224) {
                value = 192;
            } else if (value > 227 && value <= 255) {
                value = 224;
            } else if (value < 128 || ((value > 128 && value < 192) || value > 255)) {
                value = 224;
            }
            this.mHeaderMap.put(Integer.valueOf(field), Integer.valueOf(value));
        }
        if (!(128 == value || 129 == value)) {
            throw new InvalidHeaderValueException("Invalid Octet value!");
        }
        this.mHeaderMap.put(Integer.valueOf(field), Integer.valueOf(value));
    }

    public MtkEncodedStringValue[] getEncodedStringValuesEx(int field) {
        ArrayList<MtkEncodedStringValue> list = (ArrayList) this.mHeaderMap.get(Integer.valueOf(field));
        if (list == null) {
            return null;
        }
        return (MtkEncodedStringValue[]) list.toArray(new MtkEncodedStringValue[list.size()]);
    }

    public MtkEncodedStringValue getEncodedStringValueEx(int field) {
        return (MtkEncodedStringValue) this.mHeaderMap.get(Integer.valueOf(field));
    }

    public void setLongInteger(long value, int field) {
        if (field != 201) {
            MtkPduHeaders.super.setLongInteger(value, field);
            return;
        }
        Log.d(TAG, "DATE_SENT");
        this.mHeaderMap.put(Integer.valueOf(field), Long.valueOf(value));
    }
}
