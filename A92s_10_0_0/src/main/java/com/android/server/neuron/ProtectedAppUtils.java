package com.android.server.neuron;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Slog;
import java.util.ArrayList;
import libcore.io.IoUtils;

public class ProtectedAppUtils {
    private static final String AUTHORITY = "com.color.provider.SafeProvider";
    private static final Uri AUTHORITY_URI = Uri.parse("content://com.color.provider.SafeProvider");
    private static final String COLUMN_MAIN_KEY = "key";
    private static final String COLUMN_MAIN_VALUE = "value";
    private static final String COLUMN_PKG_NAME = "pkg_name";
    private static final String PROTECT_TYPE = "protect_type";
    private static final String TABLE_PP_PRIVACY_PROTECT = "pp_privacy_protect";
    private static final String TABLE_SETTINGS = "settings";
    private static final String TAG = "NeuronSystem";
    private static final Uri URI_MAIN = Uri.withAppendedPath(AUTHORITY_URI, TABLE_SETTINGS);
    private static final Uri URI_PRIVARY_PROTECT = Uri.withAppendedPath(AUTHORITY_URI, TABLE_PP_PRIVACY_PROTECT);
    private static volatile ProtectedAppUtils sInstance = null;
    private Context mContext = null;
    private Handler mHandler = null;
    private ArrayList<String> mHideIconAppList = new ArrayList<>();
    private boolean mInit = false;
    /* access modifiers changed from: private */
    public boolean mIsPrivacyOn = false;
    private ContentObserver mPrivacyDetailsObserver = null;
    private ContentObserver mPrivacySwitchObserver = null;
    private ArrayList<String> mProtectedPkgList = new ArrayList<>();

    public ProtectedAppUtils(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
    }

    public void init() {
        if (!this.mInit) {
            this.mPrivacySwitchObserver = new ContentObserver(this.mHandler) {
                /* class com.android.server.neuron.ProtectedAppUtils.AnonymousClass1 */

                public void onChange(boolean selfChange, Uri uri) {
                    boolean isPrivacySwitchOnInDB = ProtectedAppUtils.this.isPrivacySwitchOn();
                    if (isPrivacySwitchOnInDB && !ProtectedAppUtils.this.mIsPrivacyOn) {
                        boolean unused = ProtectedAppUtils.this.mIsPrivacyOn = true;
                        ProtectedAppUtils.this.updatePrivacyData(true);
                    } else if (!isPrivacySwitchOnInDB && ProtectedAppUtils.this.mIsPrivacyOn) {
                        boolean unused2 = ProtectedAppUtils.this.mIsPrivacyOn = false;
                        ProtectedAppUtils.this.updatePrivacyData(false);
                    }
                }
            };
            this.mPrivacyDetailsObserver = new ContentObserver(this.mHandler) {
                /* class com.android.server.neuron.ProtectedAppUtils.AnonymousClass2 */

                public void onChange(boolean selfChange, Uri uri) {
                    if (ProtectedAppUtils.this.mIsPrivacyOn) {
                        ProtectedAppUtils.this.updatePrivacyData(true);
                    }
                }
            };
            if (isPrivacySwitchOn()) {
                this.mIsPrivacyOn = true;
                updatePrivacyData(true);
            }
            registerPrivacyDetailsObserver();
            registerPrivacySwitchObserver();
            this.mInit = true;
            Slog.d("NeuronSystem", "ProtectedAppUtils init registerPrivacyDetailsObserver");
        }
    }

    /* access modifiers changed from: private */
    public boolean isPrivacySwitchOn() {
        boolean result = false;
        Cursor cursor = null;
        try {
            cursor = this.mContext.getContentResolver().query(URI_MAIN, new String[]{COLUMN_MAIN_VALUE}, "key= ?", new String[]{TABLE_PP_PRIVACY_PROTECT}, null);
            if (cursor == null || !cursor.moveToFirst()) {
                IoUtils.closeQuietly(cursor);
                return false;
            }
            result = "1".equals(cursor.getString(cursor.getColumnIndex(COLUMN_MAIN_VALUE)));
            IoUtils.closeQuietly(cursor);
            return result;
        } catch (Exception e) {
        } catch (Throwable th) {
            IoUtils.closeQuietly((AutoCloseable) null);
            throw th;
        }
    }

    private void registerPrivacySwitchObserver() {
        this.mContext.getContentResolver().registerContentObserver(URI_MAIN, true, this.mPrivacySwitchObserver);
    }

    private void unRegisterPrivacySwitchObserver() {
        this.mContext.getContentResolver().unregisterContentObserver(this.mPrivacySwitchObserver);
    }

    private void registerPrivacyDetailsObserver() {
        this.mContext.getContentResolver().registerContentObserver(URI_PRIVARY_PROTECT, true, this.mPrivacyDetailsObserver);
    }

    private void unRegisterPrivacyDetailsObserver() {
        this.mContext.getContentResolver().unregisterContentObserver(this.mPrivacyDetailsObserver);
    }

    /* access modifiers changed from: private */
    public void updatePrivacyData(boolean privacySwitchOn) {
        if (privacySwitchOn) {
            new ArrayList(this.mProtectedPkgList);
            Cursor cursor = null;
            try {
                cursor = this.mContext.getContentResolver().query(URI_PRIVARY_PROTECT, new String[]{COLUMN_PKG_NAME, PROTECT_TYPE}, null, null, null);
                this.mProtectedPkgList.clear();
                this.mHideIconAppList.clear();
                if (cursor != null && !cursor.isClosed() && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    do {
                        String packageName = cursor.getString(cursor.getColumnIndex(COLUMN_PKG_NAME));
                        if (!TextUtils.isEmpty(packageName)) {
                            this.mProtectedPkgList.add(packageName);
                        }
                        if (cursor.getInt(cursor.getColumnIndex(PROTECT_TYPE)) != 0) {
                            Slog.d("NeuronSystem", "ProtectedAppUtils updatePrivacyData add hide app " + packageName);
                            this.mHideIconAppList.add(packageName);
                        }
                    } while (cursor.moveToNext());
                }
            } catch (Exception e) {
            } catch (Throwable th) {
                IoUtils.closeQuietly((AutoCloseable) null);
                throw th;
            }
            IoUtils.closeQuietly(cursor);
            return;
        }
        this.mProtectedPkgList.clear();
    }

    public boolean isAppProtected(String pkg) {
        if (!this.mInit) {
            init();
        }
        if (TextUtils.isEmpty(pkg)) {
            return false;
        }
        return this.mProtectedPkgList.contains(pkg);
    }

    public boolean isAppHiden(String pkg) {
        if (!this.mInit) {
            init();
        }
        if (TextUtils.isEmpty(pkg)) {
            return false;
        }
        return this.mHideIconAppList.contains(pkg);
    }
}
