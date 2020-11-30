package com.mediatek.internal.telephony.gsm;

import android.content.Context;
import android.telephony.Rlog;
import android.telephony.SmsCbLocation;
import android.telephony.SmsCbMessage;
import android.util.Pair;
import com.android.internal.telephony.gsm.GsmSmsCbMessage;
import com.android.internal.telephony.gsm.SmsCbHeader;
import com.mediatek.internal.telephony.MtkSmsCbMessage;
import com.mediatek.internal.telephony.gsm.cbutil.Circle;
import com.mediatek.internal.telephony.gsm.cbutil.Polygon;
import com.mediatek.internal.telephony.gsm.cbutil.Shape;
import com.mediatek.internal.telephony.gsm.cbutil.Vertex;
import com.mediatek.internal.telephony.gsm.cbutil.WhamTuple;
import com.mediatek.internal.telephony.ppl.PplMessageManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class MtkGsmSmsCbMessage {
    private static final String TAG = "MtkGsmSmsCbMessage";

    public static SmsCbMessage createSmsCbMessage(Context context, SmsCbHeader header, SmsCbLocation location, byte[][] pdus) throws IllegalArgumentException {
        int pageCount = header.getNumberOfPages();
        String language = null;
        StringBuilder sb = new StringBuilder();
        MtkSmsCbHeader mtkHeader = (MtkSmsCbHeader) header;
        int priority = mtkHeader.isEmergencyMessage() ? 3 : 0;
        byte[] wac = null;
        if (pageCount > 1) {
            for (byte[] pdu : pdus) {
                Pair<String, String> p = GsmSmsCbMessage.parseBody(header, pdu);
                language = (String) p.first;
                sb.append((String) p.second);
            }
        } else if (mtkHeader.isWHAMMessage()) {
            wac = getWhamData(header, pdus[0]);
        } else {
            Pair<String, String> p2 = GsmSmsCbMessage.parseBody(header, pdus[0]);
            language = (String) p2.first;
            sb.append((String) p2.second);
            wac = getWacData(header, pdus[0]);
        }
        return new MtkSmsCbMessage(1, header.getGeographicalScope(), header.getSerialNumber(), location, header.getServiceCategory(), language, sb.toString(), priority, header.getEtwsInfo(), header.getCmasInfo(), wac);
    }

    private static byte[] getWhamData(SmsCbHeader header, byte[] pdu) {
        byte b = pdu[7 + 82];
        byte[] wham = Arrays.copyOfRange(pdu, 7, 7 + b);
        Rlog.d(TAG, "getWhamData length=" + ((int) b));
        return wham;
    }

    private static byte[] getWacData(SmsCbHeader header, byte[] pdu) {
        if (!header.isUmtsFormat()) {
            return null;
        }
        int offset = (pdu[6] * 83) + 7;
        if (offset < pdu.length - 1) {
            int length = ((pdu[offset + 1] & PplMessageManager.Type.INVALID) << 8) | (pdu[offset] & PplMessageManager.Type.INVALID);
            byte[] wac = Arrays.copyOfRange(pdu, offset + 2, offset + 2 + length);
            Rlog.d(TAG, "WAC length = " + length);
            return wac;
        }
        Rlog.d(TAG, "No WAC info.");
        return null;
    }

    /* JADX INFO: Multiple debug info for r0v4 'result'  java.util.ArrayList<com.mediatek.internal.telephony.gsm.cbutil.Shape>: [D('result' java.util.ArrayList<com.mediatek.internal.telephony.gsm.cbutil.Shape>), D('lati1' int)] */
    public static ArrayList<Shape> parseWac(MtkSmsCbMessage msg) {
        byte[] wacBytes;
        ArrayList<Shape> result = new ArrayList<>();
        byte[] wacBytes2 = msg.getWac();
        if (wacBytes2 == null) {
            return result;
        }
        int len = wacBytes2.length;
        int i = 0;
        while (i < len) {
            int tag = (wacBytes2[i] & PplMessageManager.Type.INVALID) >>> 4;
            if (tag != 1) {
                byte b = 3;
                int i2 = 2;
                if (tag == 2) {
                    int polyLen = ((wacBytes2[i] & 15) << 6) | ((wacBytes2[i + 1] & PplMessageManager.Type.INVALID) >>> 2);
                    Polygon polygon = new Polygon();
                    int count = (polyLen - 2) / 11;
                    i += 2;
                    int k = 0;
                    while (k < count) {
                        polygon.addVertex(new Vertex(((((double) ((((wacBytes2[i] & PplMessageManager.Type.INVALID) << 14) | ((wacBytes2[i + 1] & PplMessageManager.Type.INVALID) << 6)) | ((wacBytes2[i + 2] & 252) >>> i2))) * 180.0d) / 4194304.0d) - 90.0d, ((((double) (((((wacBytes2[i + 2] & b) << 20) | ((wacBytes2[i + 3] & PplMessageManager.Type.INVALID) << 12)) | ((wacBytes2[i + 4] & PplMessageManager.Type.INVALID) << 4)) | ((wacBytes2[i + 5] & 240) >>> 4))) * 360.0d) / 4194304.0d) - 180.0d));
                        polygon.addVertex(new Vertex(((((double) (((((wacBytes2[i + 5] & 15) << 18) | ((wacBytes2[i + 6] & PplMessageManager.Type.INVALID) << 10)) | ((wacBytes2[i + 7] & PplMessageManager.Type.INVALID) << 2)) | ((wacBytes2[i + 8] & 192) >>> 6))) * 180.0d) / 4194304.0d) - 90.0d, ((((double) ((((wacBytes2[i + 8] & 63) << 16) | ((wacBytes2[i + 9] & PplMessageManager.Type.INVALID) << 8)) | (wacBytes2[i + 10] & PplMessageManager.Type.INVALID))) * 360.0d) / 4194304.0d) - 180.0d));
                        i += 11;
                        k++;
                        tag = tag;
                        polyLen = polyLen;
                        result = result;
                        wacBytes2 = wacBytes2;
                        b = 3;
                        i2 = 2;
                    }
                    wacBytes = wacBytes2;
                    if ((polyLen - 2) % 11 != 0) {
                        polygon.addVertex(new Vertex(((((double) ((((wacBytes[i] & PplMessageManager.Type.INVALID) << 14) | ((wacBytes[i + 1] & PplMessageManager.Type.INVALID) << 6)) | ((wacBytes[i + 2] & 252) >>> 2))) * 180.0d) / 4194304.0d) - 90.0d, ((((double) (((((wacBytes[i + 2] & 3) << 20) | ((wacBytes[i + 3] & PplMessageManager.Type.INVALID) << 12)) | ((wacBytes[i + 4] & PplMessageManager.Type.INVALID) << 4)) | ((wacBytes[i + 5] & 240) >>> 4))) * 360.0d) / 4194304.0d) - 180.0d));
                        i += 6;
                    }
                    result = result;
                    result.add(polygon);
                } else if (tag != 3) {
                    Rlog.d(TAG, "not expected tag:" + tag);
                    i = len;
                    wacBytes = wacBytes2;
                } else {
                    result.add(new Circle(new Vertex(((((double) ((((wacBytes2[i + 2] & PplMessageManager.Type.INVALID) << 14) | ((wacBytes2[i + 3] & PplMessageManager.Type.INVALID) << 6)) | ((wacBytes2[i + 4] & 252) >>> 2))) * 180.0d) / 4194304.0d) - 90.0d, ((((double) (((((wacBytes2[i + 4] & 3) << 20) | ((wacBytes2[i + 5] & PplMessageManager.Type.INVALID) << 12)) | ((wacBytes2[i + 6] & PplMessageManager.Type.INVALID) << 4)) | ((wacBytes2[i + 7] & 240) >>> 4))) * 360.0d) / 4194304.0d) - 180.0d), ((double) ((((wacBytes2[i + 7] & 15) << 16) | ((wacBytes2[i + 8] & PplMessageManager.Type.INVALID) << 8)) | (wacBytes2[i + 9] & PplMessageManager.Type.INVALID))) / 64.0d));
                    i += 10;
                    wacBytes = wacBytes2;
                }
            } else {
                wacBytes = wacBytes2;
                int maxWaitTime = wacBytes[i + 2] & PplMessageManager.Type.INVALID;
                Rlog.d(TAG, "maxWaitTime = " + maxWaitTime);
                msg.setMaxWaitTime(maxWaitTime);
                i += 3;
            }
            wacBytes2 = wacBytes;
        }
        Iterator<Shape> it = result.iterator();
        while (it.hasNext()) {
            Rlog.d(TAG, "result=" + it.next().toString());
        }
        return result;
    }

    public static ArrayList<ArrayList<WhamTuple>> parseWHAMTupleList(MtkSmsCbMessage msg) {
        ArrayList<ArrayList<WhamTuple>> result = new ArrayList<>();
        byte[] whamBytes = msg.getWac();
        if (whamBytes == null) {
            return result;
        }
        int len = whamBytes.length;
        int i = 0;
        ArrayList<WhamTuple> singleList = null;
        while (i < len) {
            int tag = (whamBytes[i] & PplMessageManager.Type.INVALID) >>> 4;
            if (tag == 1) {
                if (singleList == null) {
                    singleList = new ArrayList<>();
                }
                int count = ((((whamBytes[i] & 15) << 3) | ((whamBytes[i + 1] & PplMessageManager.Type.INVALID) >>> 5)) - 2) / 4;
                i += 2;
                for (int k = 0; k < count; k++) {
                    singleList.add(new WhamTuple(0, ((whamBytes[i] & PplMessageManager.Type.INVALID) << 8) | (whamBytes[i + 1] & PplMessageManager.Type.INVALID), ((whamBytes[i + 2] & PplMessageManager.Type.INVALID) << 8) | (whamBytes[i + 3] & PplMessageManager.Type.INVALID)));
                    i += 4;
                }
            } else if (tag != 2) {
                Rlog.d(TAG, "not expected tag:" + tag);
                i = len;
            } else {
                ArrayList<WhamTuple> commonList = new ArrayList<>();
                int count2 = ((((whamBytes[i] & 15) << 3) | ((whamBytes[i + 1] & PplMessageManager.Type.INVALID) >>> 5)) - 2) / 4;
                i += 2;
                for (int k2 = 0; k2 < count2; k2++) {
                    commonList.add(new WhamTuple(1, ((whamBytes[i] & PplMessageManager.Type.INVALID) << 8) | (whamBytes[i + 1] & PplMessageManager.Type.INVALID), ((whamBytes[i + 2] & PplMessageManager.Type.INVALID) << 8) | (whamBytes[i + 3] & PplMessageManager.Type.INVALID)));
                    i += 4;
                }
                result.add(commonList);
            }
        }
        if (singleList != null) {
            result.add(singleList);
        }
        Iterator<ArrayList<WhamTuple>> it = result.iterator();
        while (it.hasNext()) {
            Iterator<WhamTuple> it2 = it.next().iterator();
            while (it2.hasNext()) {
                Rlog.d(TAG, "result=" + it2.next().toString());
            }
        }
        return result;
    }
}
