package com.oppo.enterprise.mdmcoreservice.service.managerimpl;

import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IOppoCustomizeService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.oppo.enterprise.mdmcoreservice.aidl.IDeviceContactManager;
import com.oppo.enterprise.mdmcoreservice.service.PermissionManager;
import com.oppo.enterprise.mdmcoreservice.utils.permission.PermissionUtils;
import java.util.ArrayList;
import java.util.List;

public class DeviceContactManagerImpl extends IDeviceContactManager.Stub {
    private static final Uri AUTHORITY_URI = Uri.parse("content://com.coloros.provider.BlackListProvider");
    private static final Uri BLACKLIST_CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "bl_list");
    private static final Uri CALL_LOG_URI = Uri.parse("content://call_log");
    private static final Uri CONTACTS_URI = Uri.parse("content://com.android.contacts");
    public static boolean MTK_GEMINI_SUPPORT;
    public static boolean OPPO_HW_MTK;
    public static boolean QUALCOMM_GEMINI_SUPPORT;
    private Context mContext;
    private IOppoCustomizeService mCustomService;
    private int mPattern = 0;

    public DeviceContactManagerImpl(Context context) {
        this.mContext = context;
        this.mCustomService = IOppoCustomizeService.Stub.asInterface(ServiceManager.getService("oppocustomize"));
        MTK_GEMINI_SUPPORT = this.mContext.getPackageManager().hasSystemFeature("mtk.gemini.support");
        QUALCOMM_GEMINI_SUPPORT = this.mContext.getPackageManager().hasSystemFeature("oppo.qualcomm.gemini.support");
        OPPO_HW_MTK = this.mContext.getPackageManager().hasSystemFeature("oppo.hw.manufacturer.mtk");
    }

    public String removeNumberPrefixAndSpecialChar(String number) {
        if (TextUtils.isEmpty(number)) {
            return null;
        }
        if (number.startsWith("+86")) {
            number = number.substring("+86".length());
        }
        return number.replaceAll("[^0-9]", "");
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceContactManager
    public boolean setContactBlockPattern(ComponentName componentName, int blockPattern) {
        PermissionManager.getInstance().checkPermission();
        if (blockPattern < 0 || blockPattern > 2) {
            return false;
        }
        try {
            return Settings.Secure.putInt(this.mContext.getContentResolver(), "oppo_comm_blacklistprovider_gov_blackwhite_pattern", blockPattern);
        } catch (Exception e) {
            Log.e("setContactBlockPattern", "msg: " + e.getMessage());
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceContactManager
    public int getContactBlockPattern(ComponentName componentName) {
        PermissionManager.getInstance().checkPermission();
        try {
            return Settings.Secure.getInt(this.mContext.getContentResolver(), "oppo_comm_blacklistprovider_gov_blackwhite_pattern", 0);
        } catch (Exception e) {
            Log.e("getContactBlockPattern", "msg: " + e.getMessage());
            return 0;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceContactManager
    public boolean setContactMatchPattern(ComponentName componentName, int matchPattern) {
        PermissionManager.getInstance().checkPermission();
        if (matchPattern < 0 || matchPattern > 4) {
            return false;
        }
        try {
            return Settings.Secure.putInt(this.mContext.getContentResolver(), "oppo_comm_blacklistprovider_gov_block_mode", matchPattern);
        } catch (Exception e) {
            Log.e("setContactMatchPattern", "msg: " + e.getMessage());
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceContactManager
    public int getContactMatchPattern(ComponentName componentName) {
        PermissionManager.getInstance().checkPermission();
        try {
            return Settings.Secure.getInt(this.mContext.getContentResolver(), "oppo_comm_blacklistprovider_gov_block_mode", 0);
        } catch (Exception e) {
            Log.e("getContactMatchPattern", "msg: " + e.getMessage());
            return 0;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceContactManager
    public boolean setContactOutgoOrIncomePattern(ComponentName componentName, int outgoOrIncomePattern) {
        PermissionManager.getInstance().checkPermission();
        if (outgoOrIncomePattern < 0 || outgoOrIncomePattern > 2) {
            return false;
        }
        try {
            return Settings.Secure.putInt(this.mContext.getContentResolver(), "oppo_comm_blacklistprovider_gov_out_or_income", outgoOrIncomePattern);
        } catch (Exception e) {
            Log.e("setContactOutgoOrIncomePattern", "msg: " + e.getMessage());
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceContactManager
    public int getContactOutgoOrIncomePattern(ComponentName componentName) {
        PermissionManager.getInstance().checkPermission();
        try {
            return Settings.Secure.getInt(this.mContext.getContentResolver(), "oppo_comm_blacklistprovider_gov_out_or_income", 2);
        } catch (Exception e) {
            Log.e("getContactOutgoOrIncomePattern ", "msg: " + e.getMessage());
            return 2;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceContactManager
    public int addContactBlockNumberList(ComponentName componentName, List<String> numbers, int blockPattern) {
        PermissionManager.getInstance().checkPermission();
        try {
            int mBlockPattern = Settings.Secure.getInt(this.mContext.getContentResolver(), "oppo_comm_blacklistprovider_gov_blackwhite_pattern", 0);
            if (blockPattern > 0 && blockPattern <= 2) {
                mBlockPattern = blockPattern;
            } else if (mBlockPattern <= 0 || blockPattern > 2) {
                return 1;
            }
            if (numbers == null || numbers.size() == 0) {
                return 2;
            }
            ArrayList<ContentProviderOperation> operationsList = new ArrayList<>();
            boolean addHasError = false;
            for (int i = 0; i < numbers.size(); i++) {
                String number = removeNumberPrefixAndSpecialChar(numbers.get(i));
                if (!TextUtils.isEmpty(number)) {
                    ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(BLACKLIST_CONTENT_URI);
                    ContentValues values = new ContentValues(3);
                    values.put("number", number);
                    values.put("block_type", (Integer) 3);
                    values.put("list_type", Integer.valueOf(mapListTypeByBlockPattern(mBlockPattern)));
                    builder.withValues(values);
                    operationsList.add(builder.build());
                    if (operationsList.size() > 50) {
                        try {
                            this.mContext.getContentResolver().applyBatch("com.coloros.provider.BlackListProvider", operationsList);
                            operationsList.clear();
                        } catch (Exception e) {
                            e.printStackTrace();
                            addHasError = true;
                        }
                    }
                }
            }
            try {
                this.mContext.getContentResolver().applyBatch("com.coloros.provider.BlackListProvider", operationsList);
                operationsList.clear();
            } catch (Exception e2) {
                Log.e("addContactBlockNumberList", "msg: " + e2.getMessage());
                addHasError = true;
            }
            if (addHasError) {
                return 3;
            }
            return 0;
        } catch (Exception e3) {
            Log.e("addContactBlockNumberList", "msg: " + e3.getMessage());
            return 4;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceContactManager
    public int removeContactBlockNumberList(ComponentName componentName, List<String> numbers, int blockPattern) {
        PermissionManager.getInstance().checkPermission();
        if (blockPattern < 0 || blockPattern > 2) {
            return 1;
        }
        if (numbers == null || numbers.size() == 0) {
            return 2;
        }
        StringBuilder numberStrBuilder = new StringBuilder();
        for (int i = 0; i < numbers.size(); i++) {
            numberStrBuilder.append("'");
            numberStrBuilder.append(numbers.get(i));
            numberStrBuilder.append("',");
        }
        String numbersStr = numberStrBuilder.toString();
        String numbersStr2 = "(" + numbersStr.substring(0, numbersStr.length() - 1) + ")";
        StringBuilder whereBuilder = new StringBuilder();
        whereBuilder.append("number IN ");
        whereBuilder.append(numbersStr2);
        int listType = mapListTypeByBlockPattern(blockPattern);
        if (listType != -1) {
            whereBuilder.append(" AND ");
            whereBuilder.append("list_type = ");
            whereBuilder.append(listType);
        }
        try {
            this.mContext.getContentResolver().delete(BLACKLIST_CONTENT_URI, whereBuilder.toString(), null);
            return 0;
        } catch (Exception e) {
            Log.e("removeContactBlockNumberList", "msg: " + e.getMessage());
            return 3;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0068, code lost:
        if (r9 != null) goto L_0x006a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x006a, code lost:
        r9.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x008b, code lost:
        if (0 == 0) goto L_0x008e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x008e, code lost:
        return r3;
     */
    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceContactManager
    public List<String> getContactBlockNumberList(ComponentName componentName, int blockPattern) {
        PermissionManager.getInstance().checkPermission();
        String selection = null;
        String[] selectionArgs = null;
        if (blockPattern > 0 && blockPattern <= 2) {
            selection = "list_type = ?";
            selectionArgs = new String[]{String.valueOf(mapListTypeByBlockPattern(blockPattern))};
        }
        String[] columns = {"number"};
        Cursor cursor = null;
        Log.i("BlackAndWhiteContactsManagerImpl", "selection:" + selection);
        List<String> numberList = new ArrayList<>();
        try {
            cursor = this.mContext.getContentResolver().query(BLACKLIST_CONTENT_URI, columns, selection, selectionArgs, null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    numberList.add(cursor.getString(0));
                }
            }
        } catch (Exception e) {
            Log.e("getContactBlockNumberList", "msg: " + e.getMessage());
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceContactManager
    public boolean removeContactBlockAllNumber(ComponentName componentName, int blockPattern) {
        PermissionManager.getInstance().checkPermission();
        int listType = mapListTypeByBlockPattern(blockPattern);
        StringBuilder whereBuilder = new StringBuilder();
        if (listType != -1) {
            whereBuilder.append("list_type = ");
            whereBuilder.append(listType);
        }
        try {
            this.mContext.getContentResolver().delete(BLACKLIST_CONTENT_URI, whereBuilder.toString(), null);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private int mapListTypeByBlockPattern(int blockPattern) {
        if (blockPattern == 1) {
            return 10;
        }
        if (blockPattern == 2) {
            return 20;
        }
        return -1;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceContactManager
    public boolean setContactsProviderWhiteList(ComponentName componentName, String packageName) {
        PermissionManager.getInstance().checkPermission();
        Log.d("BlackAndWhiteContactsManagerImpl", "setContactsProviderWhiteList, The packageName is " + packageName);
        boolean result = false;
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        try {
            Bundle resultBundle = this.mContext.getContentResolver().call(CONTACTS_URI, "setContactsProviderWhiteList", packageName, new Bundle());
            if (resultBundle != null) {
                result = resultBundle.getBoolean("result", false);
            }
        } catch (Exception e) {
            Log.e("BlackAndWhiteContactsManagerImpl", "setContactsProviderWhiteList error: " + e.getMessage());
        }
        Log.d("BlackAndWhiteContactsManagerImpl", "setContactsProviderWhiteList, result:" + result);
        return result;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceContactManager
    public boolean setContactNumberHideMode(ComponentName componentName, int mode) {
        PermissionManager.getInstance().checkPermission();
        Log.d("BlackAndWhiteContactsManagerImpl", "setContactNumberHideMode, The mode is " + mode);
        boolean result = false;
        if (mode != 1 && mode != 2) {
            return false;
        }
        try {
            result = Settings.Secure.putInt(this.mContext.getContentResolver(), "oppo_comm_contacts_numbermask_masktype", mode);
        } catch (Exception e) {
            Log.e("BlackAndWhiteContactsManagerImpl", "setContactNumberHideMode error: " + e.getMessage());
        }
        Log.d("BlackAndWhiteContactsManagerImpl", "setContactNumberHideMode, result:" + result);
        return result;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceContactManager
    public int getContactNumberHideMode(ComponentName componentName) throws RemoteException {
        PermissionManager.getInstance().checkPermission();
        Log.d("BlackAndWhiteContactsManagerImpl", "getContactNumberHideMode");
        int result = 1;
        try {
            result = Settings.Secure.getInt(this.mContext.getContentResolver(), "oppo_comm_contacts_numbermask_masktype", 1);
        } catch (Exception e) {
            Log.e("BlackAndWhiteContactsManagerImpl", "getContactNumberHideMode error: " + e.getMessage());
        }
        Log.d("BlackAndWhiteContactsManagerImpl", "getContactNumberHideMode, result:" + result);
        return result;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceContactManager
    public boolean setContactNumberMaskEnable(ComponentName componentName, int switcher) {
        PermissionManager.getInstance().checkPermission();
        Log.d("BlackAndWhiteContactsManagerImpl", "setContactNumberMaskEnable, The switcher is " + switcher);
        boolean result = false;
        if (switcher != 1 && switcher != 2) {
            return false;
        }
        try {
            result = Settings.Secure.putInt(this.mContext.getContentResolver(), "oppo_comm_contacts_numbermask_switch", switcher);
        } catch (Exception e) {
            Log.e("BlackAndWhiteContactsManagerImpl", "setContactNumberMaskEnable error: " + e.getMessage());
        }
        Log.d("BlackAndWhiteContactsManagerImpl", "setContactNumberMaskEnable, result:" + result);
        return result;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceContactManager
    public int getContactNumberMaskEnable(ComponentName componentName) throws RemoteException {
        PermissionManager.getInstance().checkPermission();
        Log.d("BlackAndWhiteContactsManagerImpl", "getContactNumberMaskEnable");
        int result = 0;
        try {
            result = Settings.Secure.getInt(this.mContext.getContentResolver(), "oppo_comm_contacts_numbermask_switch", 0);
        } catch (Exception e) {
            Log.e("BlackAndWhiteContactsManagerImpl", "getContactNumberMaskEnable error: " + e.getMessage());
        }
        Log.d("BlackAndWhiteContactsManagerImpl", "getContactNumberMaskEnable, result:" + result);
        return result;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceContactManager
    public boolean forbidCallLog(ComponentName componentName, int forbid) {
        PermissionManager.getInstance().checkPermission();
        Log.d("BlackAndWhiteContactsManagerImpl", "forbidCallLog, The forbid is " + forbid);
        boolean result = false;
        try {
            result = Settings.Secure.putInt(this.mContext.getContentResolver(), "oppo_comm_call_log_forbid_switch", forbid);
        } catch (Exception e) {
            Log.e("BlackAndWhiteContactsManagerImpl", "forbidCallLog error: " + e.getMessage());
        }
        Log.d("BlackAndWhiteContactsManagerImpl", "forbidCallLog, result:" + result);
        return result;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceContactManager
    public void forbidGetContactsPermission(ComponentName admin, String packageName) {
        PermissionManager.getInstance().checkPermission();
        long identity = Binder.clearCallingIdentity();
        try {
            PermissionUtils.grantAppPermissionWithChoice(this.mContext, packageName, "android.permission.READ_CONTACTS", 1);
        } catch (Exception e) {
            Log.e("BlackAndWhiteContactsManagerImpl", "forbidGetContactsPermission fail", e);
        }
        Binder.restoreCallingIdentity(identity);
    }
}
