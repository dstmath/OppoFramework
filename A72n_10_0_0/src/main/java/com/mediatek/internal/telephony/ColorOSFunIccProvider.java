package com.mediatek.internal.telephony;

import android.content.Context;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.Rlog;
import com.mediatek.internal.telephony.phb.IMtkIccPhoneBook;

public class ColorOSFunIccProvider {
    protected static final String[] OPPO_BOOK_ADM_CAPACITY = {"adn_max", "adn_used", "email_max", "email_used", "anr_max", "anr_used"};
    protected static final String[] OPPO_BOOK_COLUMN_NAMES = {"count"};
    protected static final int OPPO_BOOK_COLUMN_NUM = 1;
    protected static final String STR_ID = "id";
    protected static final String STR_NUMBER2 = "anr";
    public static final String TAG = "ColorOSFunIccProvider";

    private static int colorOSGetSubscription(Uri url) {
        try {
            return Integer.parseInt(url.getPathSegments().get(1));
        } catch (NumberFormatException e) {
            Rlog.d(TAG, e.toString());
            return -1;
        } catch (Exception e2) {
            Rlog.d(TAG, e2.toString());
            return -1;
        }
    }

    private static IMtkIccPhoneBook getIccPhoneBookService() {
        return IMtkIccPhoneBook.Stub.asInterface(ServiceManager.getService("mtksimphonebook"));
    }

    public static MatrixCursor colorOSMixSimAllSpace(Context context, Uri url) {
        MatrixCursor cursor = new MatrixCursor(OPPO_BOOK_COLUMN_NAMES, 1);
        int index = -1;
        try {
            int subscription = colorOSGetSubscription(url);
            IMtkIccPhoneBook iccIpb = getIccPhoneBookService();
            if (iccIpb != null) {
                index = iccIpb.colorGetSimPhonebookAllSpace(subscription);
            }
        } catch (RemoteException | SecurityException e) {
        }
        cursor.addRow(new Object[]{Integer.valueOf(index)});
        return cursor;
    }

    public static MatrixCursor colorOSMixSimUsedSpace(Context context, Uri url) {
        MatrixCursor cursor = new MatrixCursor(OPPO_BOOK_COLUMN_NAMES, 1);
        int index = -1;
        try {
            int subscription = colorOSGetSubscription(url);
            IMtkIccPhoneBook iccIpb = getIccPhoneBookService();
            if (iccIpb != null) {
                index = iccIpb.colorGetSimPhonebookUsedSpace(subscription);
            }
        } catch (RemoteException | SecurityException e) {
        }
        cursor.addRow(new Object[]{Integer.valueOf(index)});
        return cursor;
    }

    public static MatrixCursor colorOSMixSimNameLen(Context context, Uri url) {
        MatrixCursor cursor = new MatrixCursor(OPPO_BOOK_COLUMN_NAMES, 1);
        int index = -1;
        try {
            int subscription = colorOSGetSubscription(url);
            IMtkIccPhoneBook iccIpb = getIccPhoneBookService();
            if (iccIpb != null) {
                index = iccIpb.colorGetSimNameLenUsingSubId(subscription);
            }
        } catch (RemoteException | SecurityException e) {
        }
        cursor.addRow(new Object[]{Integer.valueOf(index)});
        return cursor;
    }

    public static int colorOSAddIccRecordToEfEx(int subscription, int efType, String name, String number1, String number2, String[] emails, String pin2) {
        String email;
        if (emails != null) {
            email = emails[0];
        } else {
            email = null;
        }
        try {
            IMtkIccPhoneBook iccIpb = getIccPhoneBookService();
            if (iccIpb != null) {
                return iccIpb.colorAddAdnRecordsInEfBySearchExUsingSubId(subscription, efType, "", "", name, number1, number2, pin2, email);
            }
            return -1;
        } catch (RemoteException | SecurityException e) {
            return -1;
        }
    }

    public static boolean colorOSUpdateIccRecordInEfByIdEx(int subscription, int efType, int id, String newName, String newNumber1, String newNumber2, String[] emails, String pin2) {
        String email;
        if (emails != null) {
            email = emails[0];
        } else {
            email = null;
        }
        try {
            IMtkIccPhoneBook iccIpb = getIccPhoneBookService();
            if (iccIpb != null) {
                return iccIpb.colorUpdateAdnRecordsInEfByIndexExUsingSubId(subscription, efType, newName, newNumber1, newNumber2, id, null, email);
            }
            return false;
        } catch (RemoteException | SecurityException e) {
            return false;
        }
    }

    public static MatrixCursor colorOSMSimCheckPhoneBookReady(Uri url) {
        MatrixCursor cursor = new MatrixCursor(OPPO_BOOK_COLUMN_NAMES, 1);
        boolean success = false;
        try {
            int subscription = colorOSGetSubscription(url);
            IMtkIccPhoneBook iccIpb = getIccPhoneBookService();
            if (iccIpb != null) {
                success = iccIpb.colorISPhoneBookReady(subscription);
            }
        } catch (RemoteException | SecurityException e) {
        }
        cursor.addRow(new Object[]{Integer.valueOf(success ? 1 : 0)});
        return cursor;
    }

    public static MatrixCursor colorOSMSimCheckPhoneBookPbrExist(Uri url) {
        MatrixCursor cursor = new MatrixCursor(OPPO_BOOK_COLUMN_NAMES, 1);
        boolean success = false;
        try {
            int subscription = colorOSGetSubscription(url);
            IMtkIccPhoneBook iccIpb = getIccPhoneBookService();
            if (iccIpb != null) {
                success = iccIpb.colorIsPhoneBookPbrExist(subscription);
            }
        } catch (RemoteException | SecurityException e) {
        }
        cursor.addRow(new Object[]{Integer.valueOf(success ? 1 : 0)});
        return cursor;
    }

    public static MatrixCursor colorOSMixEmailLen(Context context, Uri url) {
        MatrixCursor cursor = new MatrixCursor(OPPO_BOOK_COLUMN_NAMES, 1);
        int index = -1;
        try {
            int subscription = colorOSGetSubscription(url);
            IMtkIccPhoneBook iccIpb = getIccPhoneBookService();
            if (iccIpb != null) {
                index = iccIpb.colorGetAdnEmailLenUsingSubId(subscription);
            }
        } catch (RemoteException | SecurityException e) {
        }
        cursor.addRow(new Object[]{Integer.valueOf(index)});
        return cursor;
    }

    public static MatrixCursor colorOSMSimAdnCapacity(int subId) {
        MatrixCursor cursor = new MatrixCursor(OPPO_BOOK_ADM_CAPACITY, 1);
        try {
            IMtkIccPhoneBook iccIpb = getIccPhoneBookService();
            if (iccIpb != null) {
                int[] capacity = iccIpb.getAdnRecordsCapacityForSubscriber(subId);
                int len = OPPO_BOOK_ADM_CAPACITY.length;
                if (capacity != null && capacity.length == len) {
                    Object[] object = new Object[len];
                    for (int i = 0; i < len; i++) {
                        object[i] = Integer.valueOf(capacity[i]);
                    }
                    cursor.addRow(object);
                }
            }
        } catch (RemoteException | Exception | SecurityException e) {
        }
        return cursor;
    }

    protected static void log(String msg) {
        Rlog.d(TAG, msg);
    }
}
