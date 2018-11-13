package cm.android.mdm.manager;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.Log;
import cm.android.mdm.interfaces.IContactsManager;
import cm.android.mdm.util.MethodSignature;
import java.util.ArrayList;
import java.util.List;

public class ContactsManager implements IContactsManager {
    private static final String AUTHORITY = "com.coloros.provider.BlackListProvider";
    private static final Uri AUTHORITY_URI = Uri.parse("content://com.coloros.provider.BlackListProvider");
    private static final Uri BLACKLIST_CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, BLACKLIST_TABLE);
    private static final int BLACKLIST_MODE = 1;
    private static final String BLACKLIST_TABLE = "bl_list";
    private static final String BLOCK_TYPE = "block_type";
    private static final int BLOCK_TYPE_BOTH = 3;
    private static final String CONTACTID = "contactID";
    private static final String LIST_TYPE = "list_type";
    private static final int NORMAL_MODE = 0;
    private static final String NUMBER = "number";
    private static final String TAG = "ContactsManager";
    private static final int WHITELIST_MODE = 2;
    private static final String ZHENGQI_BLACKWHITE_PATTERN = "zhengqi_blackwhite_pattern";
    private static final int ZHENGQI_LIST_TYPE_BLACK = 10;
    private static final int ZHENGQI_LIST_TYPE_WHITE = 20;
    private boolean DEBUG = true;
    private Context mContext;
    private int mPattern = NORMAL_MODE;

    public ContactsManager(Context context) {
        this.mContext = context;
    }

    public void setContactsRestriction(int pattern) {
        Log.d(TAG, "setContactsRestriction, the pattern is " + pattern);
        this.mPattern = pattern;
        try {
            System.putInt(this.mContext.getContentResolver(), ZHENGQI_BLACKWHITE_PATTERN, pattern);
        } catch (Exception e) {
            Log.e("setContactsRestriction", "msg: " + e.getMessage());
        }
    }

    public void addContactsRestriction(int pattern, List<String> list) {
        if (this.mPattern != pattern) {
            this.mPattern = pattern;
            setContactsRestriction(pattern);
        }
        ArrayList<ContentProviderOperation> operationsList = new ArrayList();
        for (int i = NORMAL_MODE; i < list.size(); i += BLACKLIST_MODE) {
            String number = removeNumberPrefixAndSepcailChar((String) list.get(i));
            if (!TextUtils.isEmpty(number)) {
                Builder builder = ContentProviderOperation.newInsert(BLACKLIST_CONTENT_URI);
                ContentValues values = new ContentValues(BLOCK_TYPE_BOTH);
                values.put(NUMBER, number);
                values.put(BLOCK_TYPE, Integer.valueOf(BLOCK_TYPE_BOTH));
                if (pattern == BLACKLIST_MODE) {
                    values.put(LIST_TYPE, Integer.valueOf(ZHENGQI_LIST_TYPE_BLACK));
                } else if (pattern == WHITELIST_MODE) {
                    values.put(LIST_TYPE, Integer.valueOf(ZHENGQI_LIST_TYPE_WHITE));
                }
                builder.withValues(values);
                operationsList.add(builder.build());
                Log.d(TAG, "phoneList.get(i) = " + ((String) list.get(i)));
            }
        }
        try {
            this.mContext.getContentResolver().applyBatch(AUTHORITY, operationsList);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e2) {
            e2.printStackTrace();
        } catch (Exception e3) {
            e3.printStackTrace();
        }
    }

    public void removeContactsRestriction(int pattern, List<String> list) {
        if (list != null) {
            if (this.mPattern != pattern) {
                this.mPattern = pattern;
                setContactsRestriction(pattern);
            }
            String numbers = "";
            for (int i = NORMAL_MODE; i < list.size(); i += BLACKLIST_MODE) {
                numbers = numbers + "'" + ((String) list.get(i)) + "',";
            }
            numbers = "(" + numbers.substring(NORMAL_MODE, numbers.length() - 1) + ")";
            StringBuilder WhereBuilder = new StringBuilder();
            WhereBuilder.append("number IN ").append(numbers);
            Log.d(TAG, "removeContactsRestriction, the number is " + numbers);
            try {
                this.mContext.getContentResolver().delete(BLACKLIST_CONTENT_URI, WhereBuilder.toString(), null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void removeContactsRestriction(int pattern) {
        Log.d(TAG, "removeContactsRestriction, the pattern is " + pattern);
        if (this.mPattern != pattern) {
            this.mPattern = pattern;
            setContactsRestriction(pattern);
        }
        try {
            this.mContext.getContentResolver().delete(BLACKLIST_CONTENT_URI, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> getSupportMethods() {
        return MethodSignature.getMethodSignatures(ContactsManager.class);
    }

    public String removeNumberPrefixAndSepcailChar(String number) {
        if (TextUtils.isEmpty(number)) {
            return null;
        }
        if (this.DEBUG) {
            Log.d("contactsManager", "removeNumberPrefixAndSepcailChar, the number is " + number);
        }
        String prefix = "+86";
        if (number.startsWith("+86")) {
            number = number.substring("+86".length());
        }
        number = number.replaceAll("[^0-9]", "");
        if (this.DEBUG) {
            Log.d("contactsManager", "removeNumberPrefixAndSepcailChar, at last the number is " + number);
        }
        return number;
    }
}
