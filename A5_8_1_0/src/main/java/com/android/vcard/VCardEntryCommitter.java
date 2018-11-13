package com.android.vcard;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;
import java.util.ArrayList;

public class VCardEntryCommitter implements VCardEntryHandler {
    public static String LOG_TAG = "vCard";
    private final ContentResolver mContentResolver;
    private int mCounter;
    private final ArrayList<Uri> mCreatedUris = new ArrayList();
    private ArrayList<ContentProviderOperation> mOperationList;
    private long mTimeToCommit;

    public VCardEntryCommitter(ContentResolver resolver) {
        this.mContentResolver = resolver;
    }

    public void onStart() {
    }

    public void onEnd() {
        if (this.mOperationList != null) {
            this.mCreatedUris.add(pushIntoContentResolver(this.mOperationList));
        }
        if (VCardConfig.showPerformanceLog()) {
            Log.d(LOG_TAG, String.format("time to commit entries: %d ms", new Object[]{Long.valueOf(this.mTimeToCommit)}));
        }
    }

    public void onEntryCreated(VCardEntry vcardEntry) {
        long start = System.currentTimeMillis();
        this.mOperationList = vcardEntry.constructInsertOperations(this.mContentResolver, this.mOperationList);
        this.mCounter++;
        if (this.mOperationList != null && this.mOperationList.size() >= 460) {
            this.mCreatedUris.add(pushIntoContentResolver(this.mOperationList));
            this.mCounter = 0;
            this.mOperationList = null;
        }
        this.mTimeToCommit += System.currentTimeMillis() - start;
    }

    private Uri pushIntoContentResolver(ArrayList<ContentProviderOperation> operationList) {
        Uri uri = null;
        try {
            ContentProviderResult[] results = this.mContentResolver.applyBatch("com.android.contacts", operationList);
            if (!(results == null || results.length == 0 || results[0] == null)) {
                uri = results[0].uri;
            }
            return uri;
        } catch (RemoteException e) {
            Log.e(LOG_TAG, String.format("%s: %s", new Object[]{e.toString(), e.getMessage()}));
            return null;
        } catch (OperationApplicationException e2) {
            Log.e(LOG_TAG, String.format("%s: %s", new Object[]{e2.toString(), e2.getMessage()}));
            return null;
        }
    }

    public ArrayList<Uri> getCreatedUris() {
        return this.mCreatedUris;
    }
}
