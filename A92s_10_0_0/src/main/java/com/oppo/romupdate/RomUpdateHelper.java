package com.oppo.romupdate;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Pair;
import java.util.ArrayList;

public class RomUpdateHelper {
    private static final String BROADCAST_ACTION_ROM_UPDATE_CONFIG_SUCCES = "oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS";
    private static final String COLUMN_NAME_CONTENT = "xml";
    private static final String COLUMN_NAME_VERSION = "version";
    private static final Uri CONTENT_URI_WHITE_LIST = Uri.parse("content://com.nearme.romupdate.provider.db/update_list");
    /* access modifiers changed from: private */
    public static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final String OPPO_COMPONENT_SAFE_PERMISSION = "oppo.permission.OPPO_COMPONENT_SAFE";
    private static final String ROM_UPDATE_CONFIG_LIST = "ROM_UPDATE_CONFIG_LIST";
    private static final String TAG = "RomUpdateHelper";
    private Context mContext;
    /* access modifiers changed from: private */
    public String mFilterName;
    /* access modifiers changed from: private */
    public Handler mHandler;
    /* access modifiers changed from: private */
    public UpdateInfoListener mListener;

    public interface UpdateInfoListener {
        void onUpdateInfoChanged(String str);
    }

    public RomUpdateHelper(Context context, String filterName) {
        if (context == null || filterName == null) {
            throw new IllegalArgumentException("The parameters must not be null");
        }
        this.mContext = context;
        this.mFilterName = filterName;
        this.mHandler = new Handler(context.getMainLooper());
    }

    public void setUpdateInfoListener(UpdateInfoListener listener) {
        this.mListener = listener;
    }

    public void registerUpdateBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.oppo.romupdate.RomUpdateHelper.AnonymousClass1 */

            /* JADX WARNING: Code restructure failed: missing block: B:10:0x0040, code lost:
                r3 = r6.this$0.getDataFromProvider().second;
             */
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                final String content;
                if (RomUpdateHelper.DEBUG) {
                    Log.d(RomUpdateHelper.TAG, "Filter = " + RomUpdateHelper.this.mFilterName + ", onReceive intent = " + intent);
                }
                if (intent != null) {
                    try {
                        ArrayList<String> configs = intent.getStringArrayListExtra("ROM_UPDATE_CONFIG_LIST");
                        if (configs != null && configs.contains(RomUpdateHelper.this.mFilterName) && content != null && RomUpdateHelper.this.mListener != null) {
                            RomUpdateHelper.this.mHandler.post(new Runnable() {
                                /* class com.oppo.romupdate.RomUpdateHelper.AnonymousClass1.AnonymousClass1 */

                                public void run() {
                                    if (RomUpdateHelper.this.mListener != null) {
                                        RomUpdateHelper.this.mListener.onUpdateInfoChanged(content);
                                    }
                                }
                            });
                        }
                    } catch (Exception e) {
                        Log.e(RomUpdateHelper.TAG, "onReceive" + RomUpdateHelper.this.mFilterName, e);
                    }
                }
            }
        }, filter, "oppo.permission.OPPO_COMPONENT_SAFE", null);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0055, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0056, code lost:
        if (r4 != null) goto L_0x0058;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:?, code lost:
        r4.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x005c, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x005d, code lost:
        r2.addSuppressed(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0060, code lost:
        throw r3;
     */
    public Pair<Integer, String> getDataFromProvider() {
        int configVersion = -1;
        String content = null;
        String[] projection = {"version", COLUMN_NAME_CONTENT};
        try {
            ContentResolver contentResolver = this.mContext.getContentResolver();
            Uri uri = CONTENT_URI_WHITE_LIST;
            Cursor cursor = contentResolver.query(uri, projection, "filtername=\"" + this.mFilterName + "\"", null, null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    int versionColumnIndex = cursor.getColumnIndex("version");
                    int xmlColumnIndex = cursor.getColumnIndex(COLUMN_NAME_CONTENT);
                    if (cursor.moveToNext()) {
                        configVersion = cursor.getInt(versionColumnIndex);
                        content = cursor.getString(xmlColumnIndex);
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "getDataFromProvider", e);
        }
        return new Pair<>(Integer.valueOf(configVersion), content);
    }
}
