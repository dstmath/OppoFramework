package com.oppo.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.wifi.WifiEnterpriseConfig;
import android.provider.ContactsContract;
import com.android.internal.content.NativeLibraryHelper;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Locale;

public class OppoSpecialNumberUtils {
    private static final String[] CALLER_ID_PROJECTION = {"number", OppoSpecialNumColumns.EN_NAME, OppoSpecialNumColumns.CN_NAME, OppoSpecialNumColumns.TW_NAME, OppoSpecialNumColumns.PHOTO_DATA};
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
        String mNumber = number.replace(NativeLibraryHelper.CLEAR_ABI_OVERRIDE, "").replace(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER, "");
        Cursor cursorOriginal = null;
        try {
            ContentResolver contentResolver = this.mContext.getContentResolver();
            Uri uri = SPECIAL_NUMBER_CONTENT_URI;
            String[] strArr = CALLER_ID_PROJECTION;
            Cursor cursorOriginal2 = contentResolver.query(uri, strArr, "number='" + mNumber + "'", null, null);
            if (cursorOriginal2 == null) {
                if (cursorOriginal2 != null) {
                    cursorOriginal2.close();
                }
                return false;
            } else if (cursorOriginal2.moveToFirst()) {
                if (this.mLanguageInt == 1) {
                    this.mName = cursorOriginal2.getString(cursorOriginal2.getColumnIndex(OppoSpecialNumColumns.EN_NAME));
                } else if (this.mLanguageInt == 2) {
                    this.mName = cursorOriginal2.getString(cursorOriginal2.getColumnIndex(OppoSpecialNumColumns.CN_NAME));
                } else if (this.mLanguageInt == 3) {
                    this.mName = cursorOriginal2.getString(cursorOriginal2.getColumnIndex(OppoSpecialNumColumns.TW_NAME));
                }
                this.mImagedata = cursorOriginal2.getBlob(cursorOriginal2.getColumnIndex(OppoSpecialNumColumns.PHOTO_DATA));
                cursorOriginal2.close();
                return true;
            } else {
                cursorOriginal2.close();
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
        Cursor cursorOriginal = null;
        try {
            Cursor cursorOriginal2 = this.mContext.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[]{"display_name"}, "data1='" + number + "'", null, null);
            if (cursorOriginal2 == null) {
                if (cursorOriginal2 != null) {
                    cursorOriginal2.close();
                }
                return true;
            } else if (cursorOriginal2.moveToFirst()) {
                cursorOriginal2.close();
                return false;
            } else {
                cursorOriginal2.close();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (cursorOriginal != null) {
                cursorOriginal.close();
            }
            return true;
        } catch (Throwable th) {
            if (cursorOriginal != null) {
                cursorOriginal.close();
            }
            throw th;
        }
    }

    public String getNameOfnumber() {
        return this.mName;
    }

    public InputStream getInputStreamImageOfnumber() {
        byte[] bArr = this.mImagedata;
        if (bArr == null) {
            return null;
        }
        return new ByteArrayInputStream(bArr);
    }

    public Drawable getImageOfnumber() {
        byte[] bArr = this.mImagedata;
        if (bArr == null) {
            return null;
        }
        return new BitmapDrawable(BitmapFactory.decodeByteArray(bArr, 0, bArr.length));
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
