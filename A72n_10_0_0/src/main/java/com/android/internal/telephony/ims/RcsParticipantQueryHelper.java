package com.android.internal.telephony.ims;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Telephony;
import android.telephony.ims.RcsParticipantQueryResultParcelable;
import android.telephony.ims.RcsQueryContinuationToken;
import java.util.ArrayList;
import java.util.List;

/* access modifiers changed from: package-private */
public class RcsParticipantQueryHelper {
    private final ContentResolver mContentResolver;

    RcsParticipantQueryHelper(ContentResolver contentResolver) {
        this.mContentResolver = contentResolver;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x005b, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x005c, code lost:
        if (r2 != null) goto L_0x005e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0062, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0063, code lost:
        r3.addSuppressed(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0066, code lost:
        throw r4;
     */
    public RcsParticipantQueryResultParcelable performParticipantQuery(Bundle bundle) throws RemoteException {
        RcsQueryContinuationToken continuationToken = null;
        List<Integer> participantList = new ArrayList<>();
        Cursor cursor = this.mContentResolver.query(Telephony.RcsColumns.RcsParticipantColumns.RCS_PARTICIPANT_URI, null, bundle, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                participantList.add(Integer.valueOf(cursor.getInt(cursor.getColumnIndex("rcs_participant_id"))));
            }
            Bundle cursorExtras = cursor.getExtras();
            if (cursorExtras != null) {
                continuationToken = (RcsQueryContinuationToken) cursorExtras.getParcelable("query_continuation_token");
            }
            cursor.close();
            return new RcsParticipantQueryResultParcelable(continuationToken, participantList);
        }
        throw new RemoteException("Could not perform participant query, bundle: " + bundle);
    }

    static Uri getUriForParticipant(int participantId) {
        return Uri.withAppendedPath(Telephony.RcsColumns.RcsParticipantColumns.RCS_PARTICIPANT_URI, Integer.toString(participantId));
    }
}
