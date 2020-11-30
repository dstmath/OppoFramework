package com.mediatek.internal.telephony;

import android.content.ComponentName;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.CallerInfo;
import mediatek.telephony.MtkTelephony;

public class MtkCallerInfo extends CallerInfo {
    public static CallerInfo getCallerInfo(Context context, Uri contactRef, Cursor cursor) {
        MtkCallerInfo info = new MtkCallerInfo();
        info.photoResource = 0;
        info.phoneLabel = null;
        info.numberType = 0;
        info.numberLabel = null;
        info.cachedPhoto = null;
        info.isCachedPhotoCurrent = false;
        info.contactExists = false;
        info.userType = 0;
        if (VDBG) {
            Rlog.v("CallerInfo", "getCallerInfo() based on cursor...");
        }
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex("display_name");
                if (columnIndex != -1) {
                    info.name = cursor.getString(columnIndex);
                }
                int columnIndex2 = cursor.getColumnIndex(MtkTelephony.SmsCb.CbChannel.NUMBER);
                if (columnIndex2 != -1) {
                    info.phoneNumber = cursor.getString(columnIndex2);
                }
                int columnIndex3 = cursor.getColumnIndex("normalized_number");
                if (columnIndex3 != -1) {
                    info.normalizedNumber = cursor.getString(columnIndex3);
                }
                int columnIndex4 = cursor.getColumnIndex("label");
                if (columnIndex4 != -1) {
                    int typeColumnIndex = cursor.getColumnIndex("type");
                    if (typeColumnIndex != -1) {
                        info.numberType = cursor.getInt(typeColumnIndex);
                        info.numberLabel = cursor.getString(columnIndex4);
                        info.phoneLabel = ContactsContract.CommonDataKinds.Phone.getDisplayLabel(context, info.numberType, info.numberLabel).toString();
                    }
                }
                int columnIndex5 = getColumnIndexForPersonId(contactRef, cursor);
                if (columnIndex5 != -1) {
                    long contactId = cursor.getLong(columnIndex5);
                    if (contactId != 0 && !ContactsContract.Contacts.isEnterpriseContactId(contactId)) {
                        info.contactIdOrZero = contactId;
                        if (VDBG) {
                            Rlog.v("CallerInfo", "==> got info.contactIdOrZero: " + info.contactIdOrZero);
                        }
                    }
                    if (ContactsContract.Contacts.isEnterpriseContactId(contactId)) {
                        info.userType = 1;
                    }
                } else {
                    Rlog.w("CallerInfo", "Couldn't find contact_id column for " + contactRef);
                }
                int columnIndex6 = cursor.getColumnIndex("lookup");
                if (columnIndex6 != -1) {
                    info.lookupKey = cursor.getString(columnIndex6);
                }
                int columnIndex7 = cursor.getColumnIndex("photo_uri");
                if (columnIndex7 == -1 || cursor.getString(columnIndex7) == null) {
                    info.contactDisplayPhotoUri = null;
                } else {
                    info.contactDisplayPhotoUri = Uri.parse(cursor.getString(columnIndex7));
                }
                int columnIndex8 = cursor.getColumnIndex("preferred_phone_account_component_name");
                if (!(columnIndex8 == -1 || cursor.getString(columnIndex8) == null)) {
                    info.preferredPhoneAccountComponent = ComponentName.unflattenFromString(cursor.getString(columnIndex8));
                }
                int columnIndex9 = cursor.getColumnIndex("preferred_phone_account_id");
                if (!(columnIndex9 == -1 || cursor.getString(columnIndex9) == null)) {
                    info.preferredPhoneAccountId = cursor.getString(columnIndex9);
                }
                int columnIndex10 = cursor.getColumnIndex("custom_ringtone");
                if (columnIndex10 == -1 || cursor.getString(columnIndex10) == null) {
                    info.contactRingtoneUri = null;
                } else if (TextUtils.isEmpty(cursor.getString(columnIndex10))) {
                    info.contactRingtoneUri = Uri.EMPTY;
                } else {
                    info.contactRingtoneUri = Uri.parse(cursor.getString(columnIndex10));
                }
                int columnIndex11 = cursor.getColumnIndex("send_to_voicemail");
                info.shouldSendToVoicemail = columnIndex11 != -1 && cursor.getInt(columnIndex11) == 1;
                info.contactExists = true;
            }
            while (!info.shouldSendToVoicemail && cursor.moveToNext()) {
                int columnIndex12 = cursor.getColumnIndex("send_to_voicemail");
                info.shouldSendToVoicemail = columnIndex12 != -1 && cursor.getInt(columnIndex12) == 1;
            }
            cursor.close();
        }
        info.needUpdate = false;
        info.name = normalize(info.name);
        info.contactRefUri = contactRef;
        return info;
    }

    public static CallerInfo getCallerInfo(Context context, String number, int subId) {
        if (TextUtils.isEmpty(number)) {
            return null;
        }
        Rlog.d("CallerInfo", "number xxxxxx subId: " + subId);
        if (PhoneNumberUtils.isLocalEmergencyNumber(context, number)) {
            return new MtkCallerInfo().markAsEmergency(context);
        }
        if (PhoneNumberUtils.isVoiceMailNumber(subId, number)) {
            return new MtkCallerInfo().markAsVoiceMail(subId);
        }
        CallerInfo info = doSecondaryLookupIfNecessary(context, number, getCallerInfo(context, Uri.withAppendedPath(ContactsContract.PhoneLookup.ENTERPRISE_CONTENT_FILTER_URI, Uri.encode(number))));
        if (TextUtils.isEmpty(info.phoneNumber)) {
            info.phoneNumber = number;
        }
        return info;
    }
}
