package com.mediatek.storage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;

public class StorageDefaultPathDialog extends AlertActivity implements DialogInterface.OnClickListener {
    private static final String INSERT_OTG = "insert_otg";
    private static final String SD_ACTION = "android.intent.action.MEDIA_BAD_REMOVAL";
    private static final String TAG = "StorageDefaultPathDialog";
    private Boolean mInsertOtg = false;
    private BroadcastReceiver mReceiver;
    private IntentFilter mSDCardStateFilter;
    private final BroadcastReceiver mSDStateReceiver = new BroadcastReceiver() {
        /* class com.mediatek.storage.StorageDefaultPathDialog.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(StorageDefaultPathDialog.SD_ACTION)) {
                StorageDefaultPathDialog.this.finish();
            }
        }
    };
    String path = null;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        StorageDefaultPathDialog.super.onCreate(savedInstanceState);
        Log.d(TAG, "StorageDefaultPathDialog onCreate()");
        this.mSDCardStateFilter = new IntentFilter(SD_ACTION);
        this.mSDCardStateFilter.addDataScheme("file");
        this.mReceiver = this.mSDStateReceiver;
        this.mInsertOtg = Boolean.valueOf(getIntent().getBooleanExtra(INSERT_OTG, false));
        createDialog();
    }

    private void createDialog() {
        String str;
        AlertController.AlertParams p = this.mAlertParams;
        if (this.mInsertOtg.booleanValue()) {
            str = getString(134545539);
        } else {
            str = getString(134545527);
        }
        p.mTitle = str;
        p.mView = createView();
        p.mViewSpacingSpecified = true;
        p.mViewSpacingLeft = 15;
        p.mViewSpacingRight = 15;
        p.mViewSpacingTop = 5;
        p.mViewSpacingBottom = 5;
        p.mPositiveButtonText = getString(17039379);
        p.mPositiveButtonListener = this;
        p.mNegativeButtonText = getString(17039369);
        p.mNegativeButtonListener = this;
        setupAlert();
    }

    /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: com.mediatek.storage.StorageDefaultPathDialog */
    /* JADX WARN: Multi-variable type inference failed */
    private View createView() {
        TextView messageView = new TextView(this);
        messageView.setTextAppearance(messageView.getContext(), 16973892);
        messageView.setText(134545528);
        return messageView;
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        StorageDefaultPathDialog.super.onResume();
        registerReceiver(this.mReceiver, this.mSDCardStateFilter);
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        StorageDefaultPathDialog.super.onDestroy();
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        StorageDefaultPathDialog.super.onPause();
        Log.e(TAG, "onPause entry");
        unregisterReceiver(this.mReceiver);
    }

    private void onOK() {
        Intent intent = new Intent();
        intent.setAction("android.settings.INTERNAL_STORAGE_SETTINGS");
        intent.setFlags(1409286144);
        Log.d(TAG, "onOK() start activity");
        startActivity(intent);
        finish();
    }

    private void onCancel() {
        finish();
    }

    public void onClick(DialogInterface dialog, int which) {
        if (which == -2) {
            onCancel();
        } else if (which == -1) {
            onOK();
        }
    }
}
