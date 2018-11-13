package com.oppo.util;

import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Locale;

public class OppoSpecialNumberUtils {
    private static final String[] CALLER_ID_PROJECTION = new String[]{OppoSpecialNumColumns.NUMBER, OppoSpecialNumColumns.EN_NAME, OppoSpecialNumColumns.CN_NAME, OppoSpecialNumColumns.TW_NAME, OppoSpecialNumColumns.PHOTO_DATA};
    public static final Uri SPECIAL_NUMBER_CONTENT_URI = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, SpecialNumberTable);
    public static final String SpecialNumberTable = "special_contacts";
    private static final String TAG = "OppoSpecialNumberUtils";
    private final int EN_LANGUAGE = 1;
    private final int TW_LANGUAGE = 3;
    private final int ZH_LANGUAGE = 2;
    private Context mContext;
    private byte[] mImagedata = null;
    private String mLanguage = null;
    private int mLanguageInt = 1;
    private Locale mLocale = null;
    private String mName = null;

    public static class OppoSpecialNumColumns {
        public static final String CN_NAME = "cn_name";
        public static final String EN_NAME = "en_name";
        public static final String NUMBER = "number";
        public static final String OPPO_URL = "oppo_url";
        public static final String PHOTO_DATA = "photo_data";
        public static final String TW_NAME = "tw_name";
        public static final String _ID = "_id";
    }

    public OppoSpecialNumberUtils(Context context) {
        this.mContext = context;
        getLanguage();
    }

    public boolean numberNeedSpecialHandle(String number) {
        if (isSpecialNumber(number) && isNumberStoredInContacts(number)) {
            return true;
        }
        return false;
    }

    public boolean isSpecialNumber(String number) {
        if (number == null || "" == number) {
            return false;
        }
        Cursor cursorOriginal = null;
        try {
            cursorOriginal = this.mContext.getContentResolver().query(SPECIAL_NUMBER_CONTENT_URI, CALLER_ID_PROJECTION, "number='" + number.replace("-", "").replace(" ", "") + "'", null, null);
            if (cursorOriginal == null) {
                if (cursorOriginal != null) {
                    cursorOriginal.close();
                }
                return false;
            } else if (cursorOriginal.moveToFirst()) {
                if (this.mLanguageInt == 1) {
                    this.mName = cursorOriginal.getString(cursorOriginal.getColumnIndex(OppoSpecialNumColumns.EN_NAME));
                } else if (this.mLanguageInt == 2) {
                    this.mName = cursorOriginal.getString(cursorOriginal.getColumnIndex(OppoSpecialNumColumns.CN_NAME));
                } else if (this.mLanguageInt == 3) {
                    this.mName = cursorOriginal.getString(cursorOriginal.getColumnIndex(OppoSpecialNumColumns.TW_NAME));
                }
                this.mImagedata = cursorOriginal.getBlob(cursorOriginal.getColumnIndex(OppoSpecialNumColumns.PHOTO_DATA));
                if (cursorOriginal != null) {
                    cursorOriginal.close();
                }
                return true;
            } else {
                if (cursorOriginal != null) {
                    cursorOriginal.close();
                }
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (cursorOriginal != null) {
                cursorOriginal.close();
            }
            return false;
        } catch (Throwable th) {
            if (cursorOriginal != null) {
                cursorOriginal.close();
            }
            throw th;
        }
    }

    public boolean isNumberStoredInContacts(String number) {
        Cursor cursor = null;
        try {
            cursor = this.mContext.getContentResolver().query(Phone.CONTENT_URI, new String[]{"display_name"}, "data1='" + number + "'", null, null);
            if (cursor == null) {
                if (cursor != null) {
                    cursor.close();
                }
                return true;
            } else if (cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                return false;
            } else {
                if (cursor != null) {
                    cursor.close();
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
            return true;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    public String getNameOfnumber() {
        return this.mName;
    }

    public InputStream getInputStreamImageOfnumber() {
        if (this.mImagedata == null) {
            return null;
        }
        return new ByteArrayInputStream(this.mImagedata);
    }

    public Drawable getImageOfnumber() {
        if (this.mImagedata == null) {
            return null;
        }
        return new BitmapDrawable(BitmapFactory.decodeByteArray(this.mImagedata, 0, this.mImagedata.length));
    }

    private void getLanguage() {
        this.mLocale = Locale.getDefault();
        this.mLanguage = this.mLocale.getISO3Country();
        if (this.mLanguage.equals("CHN")) {
            this.mLanguageInt = 2;
        } else if (this.mLanguage.equals("USA")) {
            this.mLanguageInt = 1;
        } else if (this.mLanguage.equals("TWN")) {
            this.mLanguageInt = 3;
        }
    }
}
