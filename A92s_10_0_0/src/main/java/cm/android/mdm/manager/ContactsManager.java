package cm.android.mdm.manager;

import android.content.ComponentName;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import cm.android.mdm.interfaces.IContactsManager;
import cm.android.mdm.util.CustomizeServiceManager;
import java.util.ArrayList;
import java.util.List;

public class ContactsManager implements IContactsManager {
    private static final String AUTHORITY = "com.coloros.provider.BlackListProvider";
    private static final Uri AUTHORITY_URI = Uri.parse("content://com.coloros.provider.BlackListProvider");
    private static final Uri BLACKLIST_CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, BLACKLIST_TABLE);
    private static final String BLACKLIST_TABLE = "bl_list";
    private static final int BLOCK_PATTERN_BLACK_LIST = 1;
    private static final int BLOCK_PATTERN_INVALID = 0;
    private static final int BLOCK_PATTERN_WHITE_LIST = 2;
    private static final String BLOCK_TYPE = "block_type";
    private static final int BLOCK_TYPE_BOTH = 3;
    private static final Uri CONTACTS_URI = Uri.parse("content://com.android.contacts");
    public static final String GET_MDM_BLOCK_PATTERN = "getMdmBlockPattern";
    public static final String GET_MDM_MATCH_PATTERN = "getMdmMatchPattern";
    public static final String GET_MDM_OUTGO_INCOME_PATTERN = "getMdmOutgoOrIncomePattern";
    private static final int GOV_BLACK_LIST_TYPE = 10;
    private static final String GOV_BLACK_WHITE_BLOCK_PATTERN = "oppo_comm_blacklistprovider_gov_blackwhite_pattern";
    private static final String GOV_BLACK_WHITE_MATCH_PATTERN = "oppo_comm_blacklistprovider_gov_block_mode";
    private static final String GOV_BLACK_WHITE_OUT_OR_INCOME_PATTERN = "oppo_comm_blacklistprovider_gov_out_or_income";
    public static final String GOV_METHOD_RESULT_KEY = "result";
    public static final String GOV_SET_CONTACT_PROVIDER_WHITE_LIST = "setContactsProviderWhiteList";
    private static final int GOV_WHITE_LIST_TYPE = 20;
    private static final String LIST_TYPE = "list_type";
    private static final int MATCH_PATTERN_ALL = 0;
    private static final int MATCH_PATTERN_ALLOW_ALL = 4;
    private static final int MATCH_PATTERN_FUZZY = 2;
    private static final int MATCH_PATTERN_INTERCEPT_ALL = 3;
    private static final int MATCH_PATTERN_PREFIX = 1;
    private static final int MDM_NUMBER_ACTION_RESULT_LIST_ERROR = 2;
    private static final int MDM_NUMBER_ACTION_RESULT_PATTERN_ERROR = 1;
    private static final int MDM_NUMBER_ACTION_RESULT_PROCESS_ERROR = 3;
    private static final int MDM_NUMBER_ACTION_RESULT_PROVIDER_ERROR = 4;
    private static final int MDM_NUMBER_ACTION_RESULT_SUCCESS = 0;
    public static final String MDM_RESULT_KEY = "result";
    private static final String NUMBER = "number";
    private static final int OUTGO_OR_INCOME_PATTERN_ALL = 2;
    private static final int OUTGO_OR_INCOME_PATTERN_INCOME = 1;
    private static final int OUTGO_OR_INCOME_PATTERN_OUTGO = 0;
    public static final String SET_MDM_BLOCK_PATTERN = "setMdmBlockPattern";
    public static final String SET_MDM_MATCH_PATTERN = "setMdmMatchPattern";
    public static final String SET_MDM_OUTGO_INCOME_PATTERN = "setMdmOutgoOrIncomePattern";
    private static final String TAG = "ContactsManager";
    private static final int UPDATE_ONCE = 50;
    private Context mContext;

    public ContactsManager(Context context) {
        this.mContext = context;
    }

    @Override // cm.android.mdm.interfaces.IContactsManager
    public boolean setMdmBlockPattern(ComponentName componentName, int blockPattern) {
        Log.d(TAG, "setMdmBlockPattern, The blockPattern is " + blockPattern);
        if (blockPattern < 0 || blockPattern > 2) {
            return false;
        }
        boolean result = false;
        try {
            result = CustomizeServiceManager.setMdmBlockPattern(componentName, blockPattern);
        } catch (Exception e) {
            Log.e(TAG, "setMdmBlockPattern error: " + e.getMessage());
        }
        Log.d(TAG, "setMdmBlockPattern, result:" + result);
        return result;
    }

    @Override // cm.android.mdm.interfaces.IContactsManager
    public int getMdmBlockPattern(ComponentName componentName) {
        Log.d(TAG, GET_MDM_BLOCK_PATTERN);
        int blockPattern = 0;
        try {
            blockPattern = CustomizeServiceManager.getMdmBlockPattern(componentName);
        } catch (Exception e) {
            Log.e(TAG, "getMdmBlockPattern error: " + e.getMessage());
        }
        if (blockPattern < 0 || blockPattern > 2) {
            blockPattern = 0;
        }
        Log.d(TAG, "getMdmBlockPattern, result:" + blockPattern);
        return blockPattern;
    }

    @Override // cm.android.mdm.interfaces.IContactsManager
    public boolean setMdmMatchPattern(ComponentName componentName, int matchPattern) {
        Log.d(TAG, "setMdmMatchPattern, The matchPattern is " + matchPattern);
        if (matchPattern < 0 || matchPattern > 4) {
            return false;
        }
        boolean result = false;
        try {
            result = CustomizeServiceManager.setMdmMatchPattern(componentName, matchPattern);
        } catch (Exception e) {
            Log.e(TAG, "setMdmMatchPattern error: " + e.getMessage());
        }
        Log.d(TAG, "setMdmMatchPattern, result:" + result);
        return result;
    }

    @Override // cm.android.mdm.interfaces.IContactsManager
    public int getMdmMatchPattern(ComponentName componentName) {
        Log.d(TAG, GET_MDM_MATCH_PATTERN);
        int matchPattern = 0;
        try {
            matchPattern = CustomizeServiceManager.getMdmMatchPattern(componentName);
        } catch (Exception e) {
            Log.e(TAG, "getMdmMatchPattern error: " + e.getMessage());
        }
        if (matchPattern < 0 || matchPattern > 4) {
            matchPattern = 0;
        }
        Log.d(TAG, "getMdmMatchPattern, matchPattern:" + matchPattern);
        return matchPattern;
    }

    @Override // cm.android.mdm.interfaces.IContactsManager
    public boolean setMdmOutgoOrIncomePattern(ComponentName componentName, int outgoOrIncomePattern) {
        Log.d(TAG, SET_MDM_OUTGO_INCOME_PATTERN);
        if (outgoOrIncomePattern < 0 || outgoOrIncomePattern > 2) {
            return false;
        }
        boolean result = false;
        try {
            result = CustomizeServiceManager.setMdmOutgoOrIncomePattern(componentName, outgoOrIncomePattern);
        } catch (Exception e) {
            Log.e(TAG, "setMdmOutgoOrIncomePattern error: " + e.getMessage());
        }
        Log.d(TAG, "setMdmOutgoOrIncomePattern, result:" + result);
        return result;
    }

    @Override // cm.android.mdm.interfaces.IContactsManager
    public int getMdmOutgoOrIncomePattern(ComponentName componentName) {
        Log.d(TAG, GET_MDM_OUTGO_INCOME_PATTERN);
        int outGoOrInComePattern = 2;
        try {
            outGoOrInComePattern = CustomizeServiceManager.getMdmOutgoOrIncomePattern(componentName);
        } catch (Exception e) {
            Log.e(TAG, "getMdmOutgoOrIncomePattern error: " + e.getMessage());
        }
        if (outGoOrInComePattern < 0 || outGoOrInComePattern > 2) {
            outGoOrInComePattern = 2;
        }
        Log.d(TAG, "getMdmOutgoOrIncomePattern, result:" + outGoOrInComePattern);
        return outGoOrInComePattern;
    }

    @Override // cm.android.mdm.interfaces.IContactsManager
    public int addMdmNumberList(ComponentName componentName, List<String> numbers, int blockPattern) {
        Log.d(TAG, "addMdmNumberList");
        int result = -1;
        try {
            result = CustomizeServiceManager.addMdmNumberList(componentName, numbers, blockPattern);
        } catch (Exception e) {
            Log.e(TAG, "addMdmNumberList error: " + e.getMessage());
        }
        Log.d(TAG, "addMdmNumberList, result:" + result);
        return result;
    }

    @Override // cm.android.mdm.interfaces.IContactsManager
    public int removeMdmNumberList(ComponentName componentName, List<String> numbers, int blockPattern) {
        Log.d(TAG, "removeMdmNumberList");
        int result = -1;
        try {
            result = CustomizeServiceManager.removeMdmNumberList(componentName, numbers, blockPattern);
        } catch (Exception e) {
            Log.e(TAG, "removeMdmNumberList error: " + e.getMessage());
        }
        Log.d(TAG, "removeMdmNumberList, result:" + result);
        return result;
    }

    @Override // cm.android.mdm.interfaces.IContactsManager
    public List<String> getMdmNumberList(ComponentName componentName, int blockPattern) {
        Log.d(TAG, "getMdmNumberList blockPattern = :" + blockPattern);
        List<String> numberList = new ArrayList<>();
        try {
            return CustomizeServiceManager.getMdmNumberList(componentName, blockPattern);
        } catch (Exception e) {
            Log.e(TAG, "getMdmNumberList error: " + e.getMessage());
            return numberList;
        }
    }

    @Override // cm.android.mdm.interfaces.IContactsManager
    public boolean removeMdmAllNumber(ComponentName componentName, int blockPattern) {
        Log.d(TAG, "removeMdmAllNumber blockPattern=" + blockPattern);
        boolean result = false;
        try {
            result = CustomizeServiceManager.removeMdmAllNumber(componentName, blockPattern);
        } catch (Exception e) {
            Log.e(TAG, "removeMdmAllNumber error: " + e.getMessage());
        }
        Log.d(TAG, "removeMdmAllNumber, result:" + result);
        return result;
    }
}
