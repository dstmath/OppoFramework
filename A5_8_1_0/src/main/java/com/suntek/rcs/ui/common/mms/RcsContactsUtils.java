package com.suntek.rcs.ui.common.mms;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import com.suntek.mway.rcs.client.aidl.common.RcsColumns.GroupStatusColumns;
import com.suntek.mway.rcs.client.aidl.service.entity.GroupChatMember;
import com.suntek.mway.rcs.client.api.exception.ServiceDisconnectedException;
import com.suntek.mway.rcs.client.api.groupchat.GroupChatApi;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class RcsContactsUtils {
    public static final String LOCAL_PHOTO_SETTED = "local_photo_setted";
    public static final String MIMETYPE_RCS = "vnd.android.cursor.item/rcs";
    public static final String NOTIFY_CONTACT_PHOTO_CHANGE = "com.suntek.mway.rcs.NOTIFY_CONTACT_PHOTO_CHANGE";
    public static final String PHONE_PRE_CODE = "+86";

    private static class UpdatePhotosTask extends AsyncTask<Void, Void, Void> {
        private Context mContext;
        private Handler mHandler = new Handler();
        private String mNumber;

        UpdatePhotosTask(Context context, String number) {
            this.mContext = context;
            this.mNumber = number;
        }

        protected Void doInBackground(Void... params) {
            long aContactId = RcsContactsUtils.getContactIdByNumber(this.mContext, this.mNumber);
            ContentResolver resolver = this.mContext.getContentResolver();
            Cursor c = resolver.query(RawContacts.CONTENT_URI, new String[]{GroupStatusColumns._ID}, "contact_id=" + String.valueOf(aContactId), null, null);
            ArrayList<Long> rawContactIdList = new ArrayList();
            if (c != null) {
                try {
                    if (c.moveToFirst()) {
                        do {
                            long rawContactId = c.getLong(0);
                            if (!RcsContactsUtils.hasLocalSetted(resolver, rawContactId)) {
                                rawContactIdList.add(Long.valueOf(rawContactId));
                            }
                        } while (c.moveToNext());
                    }
                    if (c != null) {
                        c.close();
                    }
                } catch (Throwable th) {
                    if (c != null) {
                        c.close();
                    }
                }
            }
            return null;
        }
    }

    public static String getMyRcsRawContactId(Context context) {
        Uri uri = Uri.parse("content://com.android.contacts/profile/data/");
        Cursor cursor = context.getContentResolver().query(uri, new String[]{"raw_contact_id"}, null, null, null);
        if (cursor == null || !cursor.moveToNext()) {
            return null;
        }
        String rawContactId = cursor.getString(0);
        cursor.close();
        return rawContactId;
    }

    public static String getRawContactId(Context context, String contactId) {
        String rawContactId = null;
        Cursor cursor = context.getContentResolver().query(RawContacts.CONTENT_URI, new String[]{GroupStatusColumns._ID}, "contact_id=?", new String[]{contactId}, null);
        if (cursor != null) {
            if (cursor.moveToNext()) {
                rawContactId = cursor.getString(0);
            }
            cursor.close();
        }
        return rawContactId;
    }

    public static String getGroupChatMemberDisplayName(Context context, long groupId, String number) {
        Iterable iterable = null;
        try {
            iterable = GroupChatApi.getInstance().getMembers(groupId);
            if (iterable == null || iterable.size() == 0) {
                return number;
            }
        } catch (ServiceDisconnectedException e) {
            e.printStackTrace();
        } catch (RemoteException e2) {
            e2.printStackTrace();
        }
        for (GroupChatMember groupChatMember : iterable) {
            if (groupChatMember.getNumber().equals(number)) {
                if (TextUtils.isEmpty(groupChatMember.getAlias())) {
                    return getContactNameFromPhoneBook(context, number);
                }
                return groupChatMember.getAlias();
            }
        }
        return number;
    }

    public static String getContactNameFromPhoneBook(Context context, String phoneNum) {
        String numberW86;
        String contactName = phoneNum;
        if (phoneNum.startsWith(PHONE_PRE_CODE)) {
            numberW86 = phoneNum;
            phoneNum = phoneNum.substring(3);
        } else {
            numberW86 = PHONE_PRE_CODE + phoneNum;
        }
        String formatNumber = getAndroidFormatNumber(phoneNum);
        Cursor pCur = context.getContentResolver().query(Phone.CONTENT_URI, new String[]{"display_name"}, "data1 = ? OR data1 = ? OR data1 = ? ", new String[]{phoneNum, numberW86, formatNumber}, null);
        if (pCur != null) {
            try {
                if (pCur.moveToFirst()) {
                    contactName = pCur.getString(pCur.getColumnIndex("display_name"));
                }
            } catch (Throwable th) {
                if (pCur != null) {
                    pCur.close();
                }
            }
        }
        if (pCur != null) {
            pCur.close();
        }
        return contactName;
    }

    public static String getAndroidFormatNumber(String number) {
        if (TextUtils.isEmpty(number)) {
            return number;
        }
        number = number.replaceAll(" ", "");
        if (number.startsWith(PHONE_PRE_CODE)) {
            number = number.substring(3);
        }
        if (number.length() != 11) {
            return number;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(number.substring(0, 3));
        builder.append(" ");
        builder.append(number.substring(3, 7));
        builder.append(" ");
        builder.append(number.substring(7));
        return builder.toString();
    }

    public static void updateContactPhotosByNumber(Context context, String number) {
        new UpdatePhotosTask(context, number).execute(new Void[0]);
    }

    public static void setContactPhoto(Context context, byte[] input, Uri outputUri) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = context.getContentResolver().openAssetFileDescriptor(outputUri, "rw").createOutputStream();
            fileOutputStream.write(input);
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e2) {
            e2.printStackTrace();
            try {
                fileOutputStream.close();
            } catch (IOException e3) {
                e3.printStackTrace();
            }
        } catch (IOException e32) {
            e32.printStackTrace();
            try {
                fileOutputStream.close();
            } catch (IOException e322) {
                e322.printStackTrace();
            }
        } catch (Throwable th) {
            try {
                fileOutputStream.close();
            } catch (IOException e3222) {
                e3222.printStackTrace();
            }
            throw th;
        }
    }

    public static boolean hasLocalSetted(ContentResolver resolver, long rawContactId) {
        ContentResolver contentResolver = resolver;
        Cursor c = contentResolver.query(RawContacts.CONTENT_URI, new String[]{LOCAL_PHOTO_SETTED}, "_id = ? ", new String[]{String.valueOf(rawContactId)}, null);
        long localSetted = 0;
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    localSetted = c.getLong(0);
                }
            } catch (Throwable th) {
                c.close();
            }
        }
        c.close();
        if (localSetted == 1) {
            return true;
        }
        return false;
    }

    public static long getContactIdByNumber(Context context, String number) {
        if (TextUtils.isEmpty(number)) {
            return -1;
        }
        String numberW86 = number;
        if (number.startsWith(PHONE_PRE_CODE)) {
            numberW86 = number.substring(3);
        } else {
            numberW86 = PHONE_PRE_CODE + number;
        }
        Cursor cursor = context.getContentResolver().query(Phone.CONTENT_URI, new String[]{"contact_id"}, "data1=? OR data1=?", new String[]{number, numberW86}, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    long j = (long) cursor.getInt(0);
                    return j;
                }
                cursor.close();
            } finally {
                cursor.close();
            }
        }
        return -1;
    }
}
