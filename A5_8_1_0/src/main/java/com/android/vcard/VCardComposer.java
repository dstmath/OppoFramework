package com.android.vcard;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Entity;
import android.content.Entity.NamedContentValues;
import android.content.EntityIterator;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.RawContactsEntity;
import android.text.TextUtils;
import android.util.Log;
import com.suntek.mway.rcs.client.aidl.common.RcsColumns.GroupStatusColumns;
import com.suntek.mway.rcs.client.aidl.constant.Constants.FavoriteMessageProvider.FavoriteMessage;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class VCardComposer {
    private static final boolean DEBUG = false;
    public static final String FAILURE_REASON_FAILED_TO_GET_DATABASE_INFO = "Failed to get database information";
    public static final String FAILURE_REASON_NOT_INITIALIZED = "The vCard composer object is not correctly initialized";
    public static final String FAILURE_REASON_NO_ENTRY = "There's no exportable in the database";
    public static final String FAILURE_REASON_UNSUPPORTED_URI = "The Uri vCard composer received is not supported by the composer.";
    private static final String LOG_TAG = "VCardComposer";
    public static final String NO_ERROR = "No error";
    private static final String SHIFT_JIS = "SHIFT_JIS";
    private static final String UTF_8 = "UTF-8";
    private static final String[] sContactsProjection = new String[]{GroupStatusColumns._ID};
    private static final Map<Integer, String> sImMap = new HashMap();
    private final String mCharset;
    private final ContentResolver mContentResolver;
    private Uri mContentUriForRawContactsEntity;
    private Cursor mCursor;
    private boolean mCursorSuppliedFromOutside;
    private Entity mEntity;
    private EntityIterator mEntityIterator;
    private String mErrorReason;
    private boolean mFirstVCardEmittedInDoCoMoCase;
    private int mIdColumn;
    private boolean mInitDone;
    private final boolean mIsDoCoMo;
    private VCardPhoneNumberTranslationCallback mPhoneTranslationCallback;
    private RawContactEntitlesInfoCallback mRawContactEntitlesInfoCallback;
    private boolean mTerminateCalled;
    private final int mVCardType;

    public static class RawContactEntitlesInfo {
        public final long contactId;
        public final Uri rawContactEntitlesUri;

        public RawContactEntitlesInfo(Uri rawContactEntitlesUri, long contactId) {
            this.rawContactEntitlesUri = rawContactEntitlesUri;
            this.contactId = contactId;
        }
    }

    public interface RawContactEntitlesInfoCallback {
        RawContactEntitlesInfo getRawContactEntitlesInfo(long j);
    }

    static {
        sImMap.put(Integer.valueOf(0), VCardConstants.PROPERTY_X_AIM);
        sImMap.put(Integer.valueOf(1), VCardConstants.PROPERTY_X_MSN);
        sImMap.put(Integer.valueOf(2), VCardConstants.PROPERTY_X_YAHOO);
        sImMap.put(Integer.valueOf(6), VCardConstants.PROPERTY_X_ICQ);
        sImMap.put(Integer.valueOf(7), VCardConstants.PROPERTY_X_JABBER);
        sImMap.put(Integer.valueOf(3), VCardConstants.PROPERTY_X_SKYPE_USERNAME);
    }

    public VCardComposer(Context context) {
        this(context, VCardConfig.VCARD_TYPE_DEFAULT, null, true);
    }

    public VCardComposer(Context context, int vcardType) {
        this(context, vcardType, null, true);
    }

    public VCardComposer(Context context, int vcardType, String charset) {
        this(context, vcardType, charset, true);
    }

    public VCardComposer(Context context, int vcardType, boolean careHandlerErrors) {
        this(context, vcardType, null, careHandlerErrors);
    }

    public VCardComposer(Context context, int vcardType, String charset, boolean careHandlerErrors) {
        this(context, context.getContentResolver(), vcardType, charset, careHandlerErrors);
    }

    public VCardComposer(Context context, ContentResolver resolver, int vcardType, String charset, boolean careHandlerErrors) {
        int equalsIgnoreCase;
        this.mErrorReason = NO_ERROR;
        this.mTerminateCalled = true;
        this.mVCardType = vcardType;
        this.mContentResolver = resolver;
        this.mIsDoCoMo = VCardConfig.isDoCoMo(vcardType);
        if (TextUtils.isEmpty(charset)) {
            charset = "UTF-8";
        }
        if (VCardConfig.isVersion30(vcardType)) {
            equalsIgnoreCase = "UTF-8".equalsIgnoreCase(charset);
        } else {
            equalsIgnoreCase = 0;
        }
        boolean shouldAppendCharsetParam = equalsIgnoreCase ^ 1;
        if (this.mIsDoCoMo || shouldAppendCharsetParam) {
            if (SHIFT_JIS.equalsIgnoreCase(charset)) {
                this.mCharset = charset;
            } else if (TextUtils.isEmpty(charset)) {
                this.mCharset = SHIFT_JIS;
            } else {
                this.mCharset = charset;
            }
        } else if (TextUtils.isEmpty(charset)) {
            this.mCharset = "UTF-8";
        } else {
            this.mCharset = charset;
        }
        Log.d(LOG_TAG, "Use the charset \"" + this.mCharset + "\"");
    }

    public boolean init() {
        return init(null, null);
    }

    @Deprecated
    public boolean initWithRawContactsEntityUri(Uri contentUriForRawContactsEntity) {
        return init(Contacts.CONTENT_URI, sContactsProjection, null, null, null, contentUriForRawContactsEntity);
    }

    public boolean init(String selection, String[] selectionArgs) {
        return init(Contacts.CONTENT_URI, sContactsProjection, selection, selectionArgs, null, null);
    }

    public boolean init(Uri contentUri, String selection, String[] selectionArgs, String sortOrder) {
        return init(contentUri, sContactsProjection, selection, selectionArgs, sortOrder, null);
    }

    public boolean init(Uri contentUri, String selection, String[] selectionArgs, String sortOrder, Uri contentUriForRawContactsEntity) {
        return init(contentUri, sContactsProjection, selection, selectionArgs, sortOrder, contentUriForRawContactsEntity);
    }

    public boolean init(Uri contentUri, String[] projection, String selection, String[] selectionArgs, String sortOrder, Uri contentUriForRawContactsEntity) {
        if (!"com.android.contacts".equals(contentUri.getAuthority())) {
            this.mErrorReason = FAILURE_REASON_UNSUPPORTED_URI;
            return DEBUG;
        } else if (!initInterFirstPart(contentUriForRawContactsEntity) || !initInterCursorCreationPart(contentUri, projection, selection, selectionArgs, sortOrder) || !initInterMainPart()) {
            return DEBUG;
        } else {
            initEntityIterator();
            return initInterLastPart();
        }
    }

    public boolean init(Cursor cursor) {
        return initWithCallback(cursor, null);
    }

    public boolean initWithCallback(Cursor cursor, RawContactEntitlesInfoCallback rawContactEntitlesInfoCallback) {
        if (!initInterFirstPart(null)) {
            return DEBUG;
        }
        this.mCursorSuppliedFromOutside = true;
        this.mCursor = cursor;
        this.mRawContactEntitlesInfoCallback = rawContactEntitlesInfoCallback;
        if (initInterMainPart()) {
            return initInterLastPart();
        }
        return DEBUG;
    }

    private boolean initInterFirstPart(Uri contentUriForRawContactsEntity) {
        if (contentUriForRawContactsEntity == null) {
            contentUriForRawContactsEntity = RawContactsEntity.CONTENT_URI;
        }
        this.mContentUriForRawContactsEntity = contentUriForRawContactsEntity;
        if (!this.mInitDone) {
            return true;
        }
        Log.e(LOG_TAG, "init() is already called");
        return DEBUG;
    }

    private boolean initInterCursorCreationPart(Uri contentUri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        this.mCursorSuppliedFromOutside = DEBUG;
        this.mCursor = this.mContentResolver.query(contentUri, projection, selection, selectionArgs, sortOrder);
        if (this.mCursor != null) {
            return true;
        }
        Log.e(LOG_TAG, String.format("Cursor became null unexpectedly", new Object[0]));
        this.mErrorReason = FAILURE_REASON_FAILED_TO_GET_DATABASE_INFO;
        return DEBUG;
    }

    private boolean initInterMainPart() {
        boolean z = DEBUG;
        if (this.mCursor.getCount() == 0 || (this.mCursor.moveToFirst() ^ 1) != 0) {
            closeCursorIfAppropriate();
            return DEBUG;
        }
        this.mIdColumn = this.mCursor.getColumnIndex("contact_id");
        if (this.mIdColumn < 0) {
            this.mIdColumn = this.mCursor.getColumnIndex(GroupStatusColumns._ID);
        }
        if (this.mIdColumn >= 0) {
            z = true;
        }
        return z;
    }

    private void initEntityIterator() {
        StringBuilder selection = new StringBuilder();
        selection.append("contact_id").append(" IN (");
        do {
            selection.append(this.mCursor.getString(this.mIdColumn));
            if (!this.mCursor.isLast()) {
                selection.append(",");
            }
        } while (this.mCursor.moveToNext());
        selection.append(")");
        this.mEntityIterator = RawContacts.newEntityIterator(this.mContentResolver.query(this.mContentUriForRawContactsEntity, null, selection.toString(), null, "contact_id"));
        this.mCursor.moveToFirst();
    }

    private boolean initInterLastPart() {
        this.mInitDone = true;
        this.mTerminateCalled = DEBUG;
        return true;
    }

    public String createOneEntry() {
        return createOneEntry(null);
    }

    public String createOneEntry(Method getEntityIteratorMethod) {
        String vcard;
        if (this.mIsDoCoMo && (this.mFirstVCardEmittedInDoCoMoCase ^ 1) != 0) {
            this.mFirstVCardEmittedInDoCoMoCase = true;
        }
        if (this.mEntityIterator == null || getEntityIteratorMethod != null) {
            vcard = createOneEntryInternal(this.mCursor.getLong(this.mIdColumn), getEntityIteratorMethod);
        } else {
            vcard = createOneEntryInternalByIterator();
        }
        if (!this.mCursor.moveToNext()) {
            Log.e(LOG_TAG, "Cursor#moveToNext() returned false");
        }
        return vcard;
    }

    private String createOneEntryInternalByIterator() {
        if (this.mEntityIterator.hasNext() || this.mEntity != null) {
            ContentValues contentValues;
            String key;
            List<ContentValues> contentValuesList;
            Map<String, List<ContentValues>> contentValuesListMap = new HashMap();
            int lastContactId = -1;
            if (this.mEntity != null) {
                lastContactId = this.mEntity.getEntityValues().getAsInteger("contact_id").intValue();
                for (NamedContentValues namedContentValues : this.mEntity.getSubValues()) {
                    contentValues = namedContentValues.values;
                    key = contentValues.getAsString(FavoriteMessage.MIME_TYPE);
                    if (key != null) {
                        contentValuesList = (List) contentValuesListMap.get(key);
                        if (contentValuesList == null) {
                            contentValuesList = new ArrayList();
                            contentValuesListMap.put(key, contentValuesList);
                        }
                        contentValuesList.add(contentValues);
                    }
                }
                this.mEntity = null;
            }
            while (this.mEntityIterator.hasNext()) {
                Entity entity = (Entity) this.mEntityIterator.next();
                int contactId = entity.getEntityValues().getAsInteger("contact_id").intValue();
                if (lastContactId != -1 && contactId != lastContactId) {
                    lastContactId = contactId;
                    this.mEntity = entity;
                    break;
                }
                for (NamedContentValues namedContentValues2 : entity.getSubValues()) {
                    contentValues = namedContentValues2.values;
                    key = contentValues.getAsString(FavoriteMessage.MIME_TYPE);
                    if (key != null) {
                        contentValuesList = (List) contentValuesListMap.get(key);
                        if (contentValuesList == null) {
                            contentValuesList = new ArrayList();
                            contentValuesListMap.put(key, contentValuesList);
                        }
                        contentValuesList.add(contentValues);
                    }
                }
                lastContactId = contactId;
            }
            return buildVCard(contentValuesListMap);
        }
        Log.e(LOG_TAG, "EntityIterator#hasNext() returned false");
        return null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:39:0x0109 A:{Catch:{ IllegalArgumentException -> 0x00a6, IllegalAccessException -> 0x0086, InvocationTargetException -> 0x006c, all -> 0x007f }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String createOneEntryInternal(long contactId, Method getEntityIteratorMethod) {
        Map<String, List<ContentValues>> contentValuesListMap = new HashMap();
        EntityIterator entityIterator = null;
        try {
            Uri uri = this.mContentUriForRawContactsEntity;
            if (this.mRawContactEntitlesInfoCallback != null) {
                RawContactEntitlesInfo rawContactEntitlesInfo = this.mRawContactEntitlesInfoCallback.getRawContactEntitlesInfo(contactId);
                uri = rawContactEntitlesInfo.rawContactEntitlesUri;
                contactId = rawContactEntitlesInfo.contactId;
            }
            String selection = "contact_id=?";
            String[] selectionArgs = new String[]{String.valueOf(contactId)};
            if (getEntityIteratorMethod != null) {
                entityIterator = (EntityIterator) getEntityIteratorMethod.invoke(null, new Object[]{this.mContentResolver, uri, "contact_id=?", selectionArgs, null});
            } else {
                entityIterator = RawContacts.newEntityIterator(this.mContentResolver.query(uri, null, "contact_id=?", selectionArgs, null));
            }
        } catch (IllegalArgumentException e) {
            Log.e(LOG_TAG, "IllegalArgumentException has been thrown: " + e.getMessage());
        } catch (IllegalAccessException e2) {
            Log.e(LOG_TAG, "IllegalAccessException has been thrown: " + e2.getMessage());
        } catch (InvocationTargetException e3) {
            Log.e(LOG_TAG, "InvocationTargetException has been thrown: ", e3);
            throw new RuntimeException("InvocationTargetException has been thrown");
        } catch (Throwable th) {
            if (entityIterator != null) {
                entityIterator.close();
            }
        }
        String str;
        if (entityIterator == null) {
            Log.e(LOG_TAG, "EntityIterator is null");
            str = "";
            if (entityIterator != null) {
                entityIterator.close();
            }
            return str;
        } else if (entityIterator.hasNext()) {
            while (entityIterator.hasNext()) {
                Iterator namedContentValues$iterator = ((Entity) entityIterator.next()).getSubValues().iterator();
                while (true) {
                    if (namedContentValues$iterator.hasNext()) {
                        ContentValues contentValues = ((NamedContentValues) namedContentValues$iterator.next()).values;
                        String key = contentValues.getAsString(FavoriteMessage.MIME_TYPE);
                        if (key != null) {
                            List<ContentValues> contentValuesList = (List) contentValuesListMap.get(key);
                            if (contentValuesList == null) {
                                contentValuesList = new ArrayList();
                                contentValuesListMap.put(key, contentValuesList);
                            }
                            contentValuesList.add(contentValues);
                        }
                    }
                }
                if (entityIterator.hasNext()) {
                }
                break;
            }
            if (entityIterator != null) {
                entityIterator.close();
            }
            return buildVCard(contentValuesListMap);
        } else {
            Log.w(LOG_TAG, "Data does not exist. contactId: " + contactId);
            str = "";
            if (entityIterator != null) {
                entityIterator.close();
            }
            return str;
        }
    }

    public void setPhoneNumberTranslationCallback(VCardPhoneNumberTranslationCallback callback) {
        this.mPhoneTranslationCallback = callback;
    }

    public String buildVCard(Map<String, List<ContentValues>> contentValuesListMap) {
        if (contentValuesListMap == null) {
            Log.e(LOG_TAG, "The given map is null. Ignore and return empty String");
            return "";
        }
        VCardBuilder builder = new VCardBuilder(this.mVCardType, this.mCharset);
        builder.appendNameProperties((List) contentValuesListMap.get("vnd.android.cursor.item/name")).appendNickNames((List) contentValuesListMap.get("vnd.android.cursor.item/nickname")).appendPhones((List) contentValuesListMap.get("vnd.android.cursor.item/phone_v2"), this.mPhoneTranslationCallback).appendEmails((List) contentValuesListMap.get("vnd.android.cursor.item/email_v2")).appendPostals((List) contentValuesListMap.get("vnd.android.cursor.item/postal-address_v2")).appendOrganizations((List) contentValuesListMap.get("vnd.android.cursor.item/organization")).appendWebsites((List) contentValuesListMap.get("vnd.android.cursor.item/website"));
        if ((this.mVCardType & VCardConfig.FLAG_REFRAIN_IMAGE_EXPORT) == 0) {
            builder.appendPhotos((List) contentValuesListMap.get("vnd.android.cursor.item/photo"));
        }
        builder.appendNotes((List) contentValuesListMap.get("vnd.android.cursor.item/note")).appendEvents((List) contentValuesListMap.get("vnd.android.cursor.item/contact_event")).appendIms((List) contentValuesListMap.get("vnd.android.cursor.item/im")).appendSipAddresses((List) contentValuesListMap.get("vnd.android.cursor.item/sip_address")).appendRelation((List) contentValuesListMap.get("vnd.android.cursor.item/relation"));
        return builder.toString();
    }

    public void terminate() {
        closeCursorIfAppropriate();
        this.mTerminateCalled = true;
    }

    private void closeCursorIfAppropriate() {
        if (!(this.mCursorSuppliedFromOutside || this.mCursor == null)) {
            try {
                this.mCursor.close();
            } catch (SQLiteException e) {
                Log.e(LOG_TAG, "SQLiteException on Cursor#close(): " + e.getMessage());
            }
            this.mCursor = null;
        }
        if (this.mEntityIterator != null) {
            try {
                this.mEntityIterator.close();
            } catch (SQLiteException e2) {
                Log.e(LOG_TAG, "SQLiteException on EntityIterator#close(): " + e2.getMessage());
            }
            this.mEntityIterator = null;
        }
    }

    protected void finalize() throws Throwable {
        try {
            if (!this.mTerminateCalled) {
                Log.e(LOG_TAG, "finalized() is called before terminate() being called");
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }

    public int getCount() {
        if (this.mCursor != null) {
            return this.mCursor.getCount();
        }
        Log.w(LOG_TAG, "This object is not ready yet.");
        return 0;
    }

    public boolean isAfterLast() {
        if (this.mCursor == null) {
            Log.w(LOG_TAG, "This object is not ready yet.");
            return DEBUG;
        } else if (this.mEntityIterator == null) {
            return this.mCursor.isAfterLast();
        } else {
            boolean isAfterLast = (this.mEntityIterator.hasNext() || this.mEntity != null) ? this.mCursor.isAfterLast() : true;
            return isAfterLast;
        }
    }

    public String getErrorReason() {
        return this.mErrorReason;
    }
}
