package com.color.sau;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.util.Log;
import com.color.sau.SAUDb.UpdateInfoColumns;
import com.color.widget.ColorSAUAlertDialog;
import com.oppo.util.OppoSpecialNumberUtils.OppoSpecialNumColumns;

public class SauCheckUpdateHelper {
    private static final String[] DATABASE_NEWEST_VERSION_PROJECTION = new String[]{OppoSpecialNumColumns._ID, UpdateInfoColumns.PKG_NAME, UpdateInfoColumns.TYPE, UpdateInfoColumns.NEW_VERSION_NAME, UpdateInfoColumns.DESCRIPTION, UpdateInfoColumns.USE_OLD, UpdateInfoColumns.MD5_PATCH, UpdateInfoColumns.MD5_ALL, UpdateInfoColumns.URL, UpdateInfoColumns.SIZE, UpdateInfoColumns.ALL_SIZE, UpdateInfoColumns.FILE_NAME, UpdateInfoColumns.OLD_FILE_DIR, UpdateInfoColumns.DOWNLOAD_FINISHED, UpdateInfoColumns.PATCH_FINISHED, UpdateInfoColumns.INSTALL_FINISHED, UpdateInfoColumns.SILENT_UPDATING_STATUS};
    private static final boolean DEBUG = true;
    private static final String TAG = "SauJar";
    private static final int TEST_ALL_SIZE = 4096;
    private static final int TEST_PATCH_SIZE = 4096;
    private static final int TEST_TYPE_DIALOG_FORCE = 2;
    private static final int TEST_TYPE_DIALOG_NORMAL = 1;
    private static final int TEST_TYPE_DIALOG_PROGRESS = 3;
    private final Context mContext;
    private SauPkgUpdateInfo mUpdateInfo;

    public class CheckSauTask extends AsyncTask<String, Integer, SauPkgUpdateInfo> {
        protected SauPkgUpdateInfo doInBackground(String... params) {
            return SauCheckUpdateHelper.this.getUpdateInfoFromDB(SauCheckUpdateHelper.this.mContext, SauCheckUpdateHelper.this.mContext.getPackageName());
        }

        protected void onPostExecute(SauPkgUpdateInfo pkgInfo) {
            if (pkgInfo != null) {
                Log.d(SauCheckUpdateHelper.TAG, "GetUpdateInfoTask.onPostExecute result = " + SauCheckUpdateHelper.this.createUpdateAlertDialog(SauCheckUpdateHelper.this.mContext, pkgInfo));
            }
        }
    }

    public SauCheckUpdateHelper(Context context) {
        this.mContext = context;
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x00ca  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x00f6  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private SauPkgUpdateInfo getUpdateInfoFromDB(Context context, String pkg) {
        Throwable th;
        if (context == null || pkg == null) {
            Log.e(TAG, "Parameter is null, please check!!!");
            return null;
        }
        SauPkgUpdateInfo pkgInfo;
        Cursor cursor = context.getContentResolver().query(UpdateInfoColumns.CONTENT_URI, DATABASE_NEWEST_VERSION_PROJECTION, "pkg_name='" + pkg + "'", null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    SauPkgUpdateInfo pkgInfo2 = new SauPkgUpdateInfo();
                    try {
                        pkgInfo2.mPkg = cursor.getString(1);
                        int index = 2 + 1;
                        pkgInfo2.mType = cursor.getInt(2);
                        int index2 = index + 1;
                        pkgInfo2.mNewVerName = cursor.getString(index);
                        index = index2 + 1;
                        pkgInfo2.mDescription = cursor.getString(index2);
                        index2 = index + 1;
                        pkgInfo2.mCanUseOld = cursor.getInt(index);
                        index = index2 + 1;
                        pkgInfo2.mMd5Patch = cursor.getString(index2);
                        index2 = index + 1;
                        pkgInfo2.mMd5All = cursor.getString(index);
                        index = index2 + 1;
                        pkgInfo2.mUrl = cursor.getString(index2);
                        index2 = index + 1;
                        pkgInfo2.mPatchSize = cursor.getInt(index);
                        index = index2 + 1;
                        pkgInfo2.mAllSize = cursor.getInt(index2);
                        index2 = index + 1;
                        pkgInfo2.mFileName = cursor.getString(index);
                        index = index2 + 1;
                        pkgInfo2.mOldFileName = cursor.getString(index2);
                        index2 = index + 1;
                        pkgInfo2.mDownloadFinished = cursor.getInt(index);
                        index = index2 + 1;
                        pkgInfo2.mPatchFinished = cursor.getInt(index2);
                        index2 = index + 1;
                        pkgInfo2.mInstallFinished = cursor.getInt(index);
                        index = index2 + 1;
                        pkgInfo2.mSilentUpgrading = cursor.getInt(index2);
                        pkgInfo = pkgInfo2;
                        if (cursor != null) {
                            cursor.close();
                        }
                        if (pkgInfo != null && pkgInfo.mSilentUpgrading == 1) {
                            Log.d(TAG, pkgInfo.mPkg + " is silent upgrading, do not show dialog.");
                            pkgInfo = null;
                        }
                        return pkgInfo;
                    } catch (Throwable th2) {
                        th = th2;
                        if (cursor != null) {
                        }
                        throw th;
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                if (cursor != null) {
                    cursor.close();
                }
                throw th;
            }
        }
        pkgInfo = null;
        if (cursor != null) {
        }
        Log.d(TAG, pkgInfo.mPkg + " is silent upgrading, do not show dialog.");
        pkgInfo = null;
        return pkgInfo;
    }

    private boolean createUpdateAlertDialog(Context context, SauPkgUpdateInfo pkgInfo) {
        if (pkgInfo == null) {
            return false;
        }
        if (pkgInfo.mCanUseOld == 0) {
            createAlertDialog(context, pkgInfo);
            return DEBUG;
        } else if (!isSendUpdateInfo(context, pkgInfo.mPkg)) {
            return false;
        } else {
            createAlertDialog(context, pkgInfo);
            return DEBUG;
        }
    }

    private void createAlertDialog(Context context, SauPkgUpdateInfo pkgInfo) {
        SauAlertManager alm = new SauAlertManager();
        alm.init(context, pkgInfo);
        alm.createAlertDialog();
    }

    private boolean isSendUpdateInfo(Context context, String pkg) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        int launchTime = pref.getInt(pkg, 1);
        int checkThresh = SystemProperties.getInt("persist.sys.sau.launchcheck", 2);
        if (checkThresh <= 0) {
            checkThresh = 2;
        }
        Editor editor = pref.edit();
        if (launchTime == 0 || launchTime % checkThresh != 0) {
            editor.putInt(pkg, launchTime + 1);
            editor.commit();
            return false;
        }
        editor.putInt(pkg, 1);
        editor.commit();
        return DEBUG;
    }

    public void SauCheckUpdate() {
        if (this.mContext == null || !(this.mContext instanceof Activity) || (((Activity) this.mContext).isFinishing() ^ 1) == 0) {
            Log.d(TAG, "context is null or activity context is finishing");
            return;
        }
        new CheckSauTask().execute(new String[]{"SAU"});
    }

    public boolean SupportSauUpdate() {
        ApplicationInfo appInfo = null;
        try {
            appInfo = this.mContext.getPackageManager().getApplicationInfo("com.coloros.sau", 128);
        } catch (NameNotFoundException e) {
        }
        if (appInfo == null) {
            return false;
        }
        return DEBUG;
    }

    public boolean hasUpdate(Context context) {
        SauPkgUpdateInfo info = getUpdateInfoFromDB(context, context.getPackageName());
        if (info == null) {
            return false;
        }
        this.mUpdateInfo = info;
        return DEBUG;
    }

    public ColorSAUAlertDialog getUpgradeDialog(Context context) {
        if (!hasUpdate(context) || this.mUpdateInfo == null) {
            return null;
        }
        SauAlertManager alm = new SauAlertManager();
        alm.init(context, this.mUpdateInfo);
        return alm.getUpgradeDialog();
    }

    public boolean showUpgradeDialogForTest(Context context, int type) {
        if (type == 1 || type == 2) {
            int i;
            SauPkgUpdateInfo info = new SauPkgUpdateInfo();
            info.mPkg = "com.oppo.music";
            info.mNewVerName = "Vtest.01";
            info.mDescription = "add for test: check if the dialog shows correctly.";
            info.mPatchSize = 4096;
            info.mAllSize = 4096;
            if (type == 1) {
                i = 1;
            } else {
                i = 0;
            }
            info.mCanUseOld = i;
            info.mPatchFinished = 1;
            info.mDownloadFinished = 1;
            info.mInstallFinished = 0;
            info.mFileName = null;
            SauAlertManager alm = new SauAlertManager();
            alm.init(context, info);
            ColorSAUAlertDialog dialog = alm.getUpgradeDialog();
            if (dialog != null) {
                dialog.show();
                return DEBUG;
            }
        } else if (type == 3) {
            SauWaitProgressDialog progressDialog = new SauWaitProgressDialog(this.mContext);
            if (progressDialog != null) {
                progressDialog.show();
                return DEBUG;
            }
        }
        return false;
    }
}
