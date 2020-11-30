package com.mediatek.internal.telephony.util;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Binder;
import android.os.Build;
import android.telephony.Rlog;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.SmsMessageBase;
import com.android.internal.telephony.cdma.SmsMessage;
import com.mediatek.internal.telephony.gsm.MtkSmsMessage;
import java.util.Iterator;
import java.util.List;

public final class MtkSMSDispatcherUtil {
    private static final boolean ENG = "eng".equals(Build.TYPE);
    static final String TAG = "MtkSMSDispatcherUtil";

    private MtkSMSDispatcherUtil() {
    }

    public static SmsMessageBase.SubmitPduBase getSubmitPdu(boolean isCdma, String scAddr, String destAddr, String message, boolean statusReportRequested, SmsHeader smsHeader) {
        if (isCdma) {
            return getSubmitPduCdma(scAddr, destAddr, message, statusReportRequested, smsHeader);
        }
        return getSubmitPduGsm(scAddr, destAddr, message, statusReportRequested);
    }

    public static SmsMessageBase.SubmitPduBase getSubmitPdu(boolean isCdma, String scAddr, String destAddr, String message, boolean statusReportRequested, SmsHeader smsHeader, int priority, int validityPeriod) {
        if (isCdma) {
            return getSubmitPduCdma(scAddr, destAddr, message, statusReportRequested, smsHeader, priority);
        }
        return getSubmitPduGsm(scAddr, destAddr, message, statusReportRequested, validityPeriod);
    }

    public static SmsMessageBase.SubmitPduBase getSubmitPduGsm(String scAddr, String destAddr, String message, boolean statusReportRequested) {
        return MtkSmsMessage.getSubmitPdu(scAddr, destAddr, message, statusReportRequested);
    }

    public static SmsMessageBase.SubmitPduBase getSubmitPduGsm(String scAddr, String destAddr, String message, boolean statusReportRequested, int validityPeriod) {
        return MtkSmsMessage.getSubmitPdu(scAddr, destAddr, message, statusReportRequested, validityPeriod);
    }

    public static SmsMessageBase.SubmitPduBase getSubmitPduCdma(String scAddr, String destAddr, String message, boolean statusReportRequested, SmsHeader smsHeader) {
        return SmsMessage.getSubmitPdu(scAddr, destAddr, message, statusReportRequested, smsHeader);
    }

    public static SmsMessageBase.SubmitPduBase getSubmitPduCdma(String scAddr, String destAddr, String message, boolean statusReportRequested, SmsHeader smsHeader, int priority) {
        return SmsMessage.getSubmitPdu(scAddr, destAddr, message, statusReportRequested, smsHeader, priority);
    }

    public static SmsMessageBase.SubmitPduBase getSubmitPdu(boolean isCdma, String scAddr, String destAddr, int destPort, byte[] message, boolean statusReportRequested) {
        if (isCdma) {
            return getSubmitPduCdma(scAddr, destAddr, destPort, message, statusReportRequested);
        }
        return getSubmitPduGsm(scAddr, destAddr, destPort, message, statusReportRequested);
    }

    public static SmsMessageBase.SubmitPduBase getSubmitPduCdma(String scAddr, String destAddr, int destPort, byte[] message, boolean statusReportRequested) {
        return SmsMessage.getSubmitPdu(scAddr, destAddr, destPort, message, statusReportRequested);
    }

    public static SmsMessageBase.SubmitPduBase getSubmitPduGsm(String scAddr, String destAddr, int destPort, byte[] message, boolean statusReportRequested) {
        return MtkSmsMessage.getSubmitPdu(scAddr, destAddr, destPort, message, statusReportRequested);
    }

    public static GsmAlphabet.TextEncodingDetails calculateLength(boolean isCdma, CharSequence messageBody, boolean use7bitOnly) {
        if (isCdma) {
            return calculateLengthCdma(messageBody, use7bitOnly);
        }
        return calculateLengthGsm(messageBody, use7bitOnly);
    }

    public static GsmAlphabet.TextEncodingDetails calculateLengthGsm(CharSequence messageBody, boolean use7bitOnly) {
        return MtkSmsMessage.calculateLength(messageBody, use7bitOnly);
    }

    public static GsmAlphabet.TextEncodingDetails calculateLengthCdma(CharSequence messageBody, boolean use7bitOnly) {
        return SmsMessage.calculateLength(messageBody, use7bitOnly, false);
    }

    public static SmsMessageBase.SubmitPduBase getSubmitPdu(boolean isCdma, String scAddr, String destAddr, int destinationPort, int originalPort, byte[] data, boolean statusReportRequested) {
        if (isCdma) {
            return null;
        }
        return getSubmitPduGsm(scAddr, destAddr, destinationPort, originalPort, data, statusReportRequested);
    }

    public static SmsMessageBase.SubmitPduBase getSubmitPduGsm(String scAddr, String destAddr, int destinationPort, int originalPort, byte[] data, boolean statusReportRequested) {
        return MtkSmsMessage.getSubmitPdu(scAddr, destAddr, destinationPort, originalPort, data, statusReportRequested);
    }

    public static String getPackageNameViaProcessId(Context context, String[] packageNames) {
        String packageName = null;
        String rsp = null;
        int i = 0;
        if (packageNames.length == 1) {
            packageName = packageNames[0];
        } else if (packageNames.length > 1) {
            int callingPid = Binder.getCallingPid();
            List processList = ((ActivityManager) context.getSystemService("activity")).getRunningAppProcesses();
            if (processList != null) {
                Iterator index = processList.iterator();
                while (true) {
                    if (!index.hasNext()) {
                        break;
                    }
                    ActivityManager.RunningAppProcessInfo processInfo = index.next();
                    if (callingPid == processInfo.pid) {
                        String[] strArr = processInfo.pkgList;
                        int length = strArr.length;
                        String packageName2 = null;
                        int i2 = i;
                        while (true) {
                            if (i2 >= length) {
                                packageName = packageName2;
                                break;
                            }
                            String pkgInProcess = strArr[i2];
                            int length2 = packageNames.length;
                            int i3 = i;
                            while (true) {
                                if (i3 >= length2) {
                                    break;
                                }
                                String pkg = packageNames[i3];
                                if (pkg.equals(pkgInProcess)) {
                                    packageName2 = pkg;
                                    break;
                                }
                                i3++;
                            }
                            if (packageName2 != null) {
                                packageName = packageName2;
                                break;
                            }
                            i2++;
                            i = 0;
                        }
                    } else {
                        i = 0;
                    }
                }
            }
        }
        if (packageName != null) {
            rsp = packageName;
        } else if (packageNames.length > 0) {
            rsp = packageNames[0];
        }
        if (ENG) {
            Rlog.d(TAG, "getPackageNameViaProcessId: " + rsp);
        }
        return rsp;
    }

    public static SmsMessageBase.SubmitPduBase getSubmitPdu(boolean isCdma, String scAddress, String destinationAddress, byte[] data, byte[] smsHeaderData, boolean statusReportRequested) {
        if (!isCdma) {
            return MtkSmsMessage.getSubmitPdu(scAddress, destinationAddress, data, smsHeaderData, statusReportRequested);
        }
        return null;
    }

    public static SmsMessageBase.SubmitPduBase getSubmitPduGsm(String scAddress, String destinationAddress, byte[] data, byte[] smsHeaderData, boolean statusReportRequested) {
        return MtkSmsMessage.getSubmitPdu(scAddress, destinationAddress, data, smsHeaderData, statusReportRequested);
    }

    public static SmsMessageBase.SubmitPduBase getSubmitPdu(boolean isCdma, String scAddress, String destinationAddress, String message, boolean statusReportRequested, byte[] header, int encoding, int languageTable, int languageShiftTable, int validityPeriod) {
        if (!isCdma) {
            return getSubmitPduGsm(scAddress, destinationAddress, message, statusReportRequested, header, encoding, languageTable, languageShiftTable, validityPeriod);
        }
        return null;
    }

    public static SmsMessageBase.SubmitPduBase getSubmitPduGsm(String scAddress, String destinationAddress, String message, boolean statusReportRequested, byte[] header, int encoding, int languageTable, int languageShiftTable, int validityPeriod) {
        return MtkSmsMessage.getSubmitPdu(scAddress, destinationAddress, message, statusReportRequested, header, encoding, languageTable, languageShiftTable, validityPeriod);
    }
}
