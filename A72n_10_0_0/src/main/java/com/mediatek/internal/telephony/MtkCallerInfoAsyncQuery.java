package com.mediatek.internal.telephony;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.CallerInfo;
import com.android.internal.telephony.CallerInfoAsyncQuery;

public class MtkCallerInfoAsyncQuery extends CallerInfoAsyncQuery {
    public static CallerInfoAsyncQuery startQuery(int token, Context context, Uri contactRef, CallerInfoAsyncQuery.OnQueryCompleteListener listener, Object cookie) {
        MtkCallerInfoAsyncQuery c = new MtkCallerInfoAsyncQuery();
        c.allocate(context, contactRef);
        CallerInfoAsyncQuery.CookieWrapper cw = new CallerInfoAsyncQuery.CookieWrapper();
        cw.listener = listener;
        cw.cookie = cookie;
        cw.event = 1;
        c.mHandler.startQuery(token, cw, contactRef, (String[]) null, (String) null, (String[]) null, (String) null);
        return c;
    }

    public static CallerInfoAsyncQuery startQuery(int token, Context context, String number, CallerInfoAsyncQuery.OnQueryCompleteListener listener, Object cookie, int subId) {
        Uri contactRef = ContactsContract.PhoneLookup.ENTERPRISE_CONTENT_FILTER_URI.buildUpon().appendPath(number).appendQueryParameter("sip", String.valueOf(PhoneNumberUtils.isUriNumber(number))).build();
        MtkCallerInfoAsyncQuery c = new MtkCallerInfoAsyncQuery();
        c.allocate(context, contactRef);
        CallerInfoAsyncQuery.CookieWrapper cw = new CallerInfoAsyncQuery.CookieWrapper();
        cw.listener = listener;
        cw.cookie = cookie;
        cw.number = number;
        cw.subId = subId;
        if (PhoneNumberUtils.isLocalEmergencyNumber(context, number)) {
            cw.event = 4;
        } else if (PhoneNumberUtils.isVoiceMailNumber(context, subId, number)) {
            cw.event = 5;
        } else {
            cw.event = 1;
        }
        c.mHandler.startQuery(token, cw, contactRef, (String[]) null, (String) null, (String[]) null, (String) null);
        return c;
    }

    /* access modifiers changed from: protected */
    public void allocate(Context context, Uri contactRef) {
        if (context == null || contactRef == null) {
            throw new CallerInfoAsyncQuery.QueryPoolException("Bad context or query uri.");
        }
        this.mHandler = new MtkCallerInfoAsyncQueryHandler(context);
        this.mHandler.mQueryUri = contactRef;
    }

    public class MtkCallerInfoAsyncQueryHandler extends CallerInfoAsyncQuery.CallerInfoAsyncQueryHandler {
        protected MtkCallerInfoAsyncQueryHandler(Context context) {
            super(MtkCallerInfoAsyncQuery.this, context);
        }

        /* access modifiers changed from: protected */
        public void onQueryComplete(final int token, Object cookie, Cursor cursor) {
            Rlog.d("CallerInfoAsyncQuery", "##### onQueryComplete() #####   query complete for token: " + token);
            final CallerInfoAsyncQuery.CookieWrapper cw = (CallerInfoAsyncQuery.CookieWrapper) cookie;
            if (cw == null) {
                Rlog.i("CallerInfoAsyncQuery", "Cookie is null, ignoring onQueryComplete() request.");
                if (cursor != null) {
                    cursor.close();
                }
            } else if (cw.event == 3) {
                for (Runnable r : this.mPendingListenerCallbacks) {
                    r.run();
                }
                this.mPendingListenerCallbacks.clear();
                MtkCallerInfoAsyncQuery.this.release();
                if (cursor != null) {
                    cursor.close();
                }
            } else {
                if (cw.event == 6) {
                    if (this.mCallerInfo != null) {
                        this.mCallerInfo.geoDescription = cw.geoDescription;
                    }
                    CallerInfoAsyncQuery.CookieWrapper endMarker = new CallerInfoAsyncQuery.CookieWrapper();
                    endMarker.event = 3;
                    startQuery(token, endMarker, null, null, null, null, null);
                }
                if (this.mCallerInfo == null) {
                    if (this.mContext == null || this.mQueryUri == null) {
                        throw new CallerInfoAsyncQuery.QueryPoolException("Bad context or query uri, or CallerInfoAsyncQuery already released.");
                    }
                    if (cw.event == 4) {
                        this.mCallerInfo = new MtkCallerInfo().markAsEmergency(this.mContext);
                    } else if (cw.event == 5) {
                        this.mCallerInfo = new MtkCallerInfo().markAsVoiceMail(cw.subId);
                    } else {
                        this.mCallerInfo = MtkCallerInfo.getCallerInfo(this.mContext, this.mQueryUri, cursor);
                        CallerInfo newCallerInfo = CallerInfo.doSecondaryLookupIfNecessary(this.mContext, cw.number, this.mCallerInfo);
                        if (newCallerInfo != this.mCallerInfo) {
                            this.mCallerInfo = newCallerInfo;
                        }
                        if (!TextUtils.isEmpty(cw.number)) {
                            this.mCallerInfo.phoneNumber = PhoneNumberUtils.formatNumber(cw.number, this.mCallerInfo.normalizedNumber, CallerInfo.getCurrentCountryIso(this.mContext));
                        }
                        if (TextUtils.isEmpty(this.mCallerInfo.name)) {
                            cw.event = 6;
                            startQuery(token, cw, null, null, null, null, null);
                            return;
                        }
                    }
                    CallerInfoAsyncQuery.CookieWrapper endMarker2 = new CallerInfoAsyncQuery.CookieWrapper();
                    endMarker2.event = 3;
                    startQuery(token, endMarker2, null, null, null, null, null);
                }
                if (cw.listener != null) {
                    this.mPendingListenerCallbacks.add(new Runnable() {
                        /* class com.mediatek.internal.telephony.MtkCallerInfoAsyncQuery.MtkCallerInfoAsyncQueryHandler.AnonymousClass1 */

                        public void run() {
                            cw.listener.onQueryComplete(token, cw.cookie, MtkCallerInfoAsyncQueryHandler.this.mCallerInfo);
                        }
                    });
                } else {
                    Rlog.w("CallerInfoAsyncQuery", "There is no listener to notify for this query.");
                }
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }
}
