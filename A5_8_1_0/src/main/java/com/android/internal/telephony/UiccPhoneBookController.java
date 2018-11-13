package com.android.internal.telephony;

import android.content.ContentValues;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.Rlog;
import com.android.internal.telephony.IIccPhoneBook.Stub;
import com.android.internal.telephony.uicc.AdnRecord;
import java.util.List;

public class UiccPhoneBookController extends Stub {
    private static final String TAG = "UiccPhoneBookController";
    private Phone[] mPhone;

    public UiccPhoneBookController(Phone[] phone) {
        if (ServiceManager.getService("simphonebook") == null) {
            ServiceManager.addService("simphonebook", this);
        }
        this.mPhone = phone;
    }

    public boolean updateAdnRecordsInEfBySearch(int efid, String oldTag, String oldPhoneNumber, String newTag, String newPhoneNumber, String pin2) throws RemoteException {
        return updateAdnRecordsInEfBySearchForSubscriber(getDefaultSubscription(), efid, oldTag, oldPhoneNumber, newTag, newPhoneNumber, pin2);
    }

    public boolean updateAdnRecordsInEfBySearchForSubscriber(int subId, int efid, String oldTag, String oldPhoneNumber, String newTag, String newPhoneNumber, String pin2) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.updateAdnRecordsInEfBySearch(efid, oldTag, oldPhoneNumber, newTag, newPhoneNumber, pin2);
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return false;
    }

    public boolean updateAdnRecordsInEfByIndex(int efid, String newTag, String newPhoneNumber, int index, String pin2) throws RemoteException {
        return updateAdnRecordsInEfByIndexForSubscriber(getDefaultSubscription(), efid, newTag, newPhoneNumber, index, pin2);
    }

    public boolean updateAdnRecordsInEfByIndexForSubscriber(int subId, int efid, String newTag, String newPhoneNumber, int index, String pin2) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.updateAdnRecordsInEfByIndex(efid, newTag, newPhoneNumber, index, pin2);
        }
        Rlog.e(TAG, "updateAdnRecordsInEfByIndex iccPbkIntMgr is null for Subscription:" + subId);
        return false;
    }

    public int[] getAdnRecordsSize(int efid) throws RemoteException {
        return getAdnRecordsSizeForSubscriber(getDefaultSubscription(), efid);
    }

    public int[] getAdnRecordsSizeForSubscriber(int subId, int efid) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.getAdnRecordsSize(efid);
        }
        Rlog.e(TAG, "getAdnRecordsSize iccPbkIntMgr is null for Subscription:" + subId);
        return null;
    }

    public List<AdnRecord> getAdnRecordsInEf(int efid) throws RemoteException {
        return getAdnRecordsInEfForSubscriber(getDefaultSubscription(), efid);
    }

    public List<AdnRecord> getAdnRecordsInEfForSubscriber(int subId, int efid) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.getAdnRecordsInEf(efid);
        }
        Rlog.e(TAG, "getAdnRecordsInEf iccPbkIntMgr isnull for Subscription:" + subId);
        return null;
    }

    public int[] getAdnRecordsCapacity() throws RemoteException {
        return getAdnRecordsCapacityForSubscriber(getDefaultSubscription());
    }

    public int[] getAdnRecordsCapacityForSubscriber(int subId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.getAdnRecordsCapacity();
        }
        Rlog.e(TAG, "getAdnRecordsCapacity iccPbkIntMgr is null for Subscription:" + subId);
        return null;
    }

    public boolean updateAdnRecordsWithContentValuesInEfBySearch(int efid, ContentValues values, String pin2) throws RemoteException {
        return updateAdnRecordsWithContentValuesInEfBySearchUsingSubId(getDefaultSubscription(), efid, values, pin2);
    }

    public boolean updateAdnRecordsWithContentValuesInEfBySearchUsingSubId(int subId, int efid, ContentValues values, String pin2) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.updateAdnRecordsWithContentValuesInEfBySearch(efid, values, pin2);
        }
        Rlog.e(TAG, "updateAdnRecordsWithContentValuesInEfBySearchUsingSubId iccPbkIntMgr is null for Subscription:" + subId);
        return false;
    }

    private IccPhoneBookInterfaceManager getIccPhoneBookInterfaceManager(int subId) {
        try {
            return this.mPhone[SubscriptionController.getInstance().getPhoneId(subId)].getIccPhoneBookInterfaceManager();
        } catch (NullPointerException e) {
            Rlog.e(TAG, "Exception is :" + e.toString() + " For subscription :" + subId);
            e.printStackTrace();
            return null;
        } catch (ArrayIndexOutOfBoundsException e2) {
            Rlog.e(TAG, "Exception is :" + e2.toString() + " For subscription :" + subId);
            e2.printStackTrace();
            return null;
        }
    }

    private int getDefaultSubscription() {
        return PhoneFactory.getDefaultSubscription();
    }

    private IccPhoneBookInterfaceManager getIccPhoneBookInterfaceManagerOppo(int slotId) {
        try {
            return this.mPhone[slotId].getIccPhoneBookInterfaceManager();
        } catch (NullPointerException e) {
            Rlog.e(TAG, "Exception is :" + e.toString() + " For slotId :" + slotId);
            e.printStackTrace();
            return null;
        } catch (ArrayIndexOutOfBoundsException e2) {
            Rlog.e(TAG, "Exception is :" + e2.toString() + " For slotId :" + slotId);
            e2.printStackTrace();
            return null;
        }
    }

    public int colorGetSimPhonebookAllSpace(int slotId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManagerOppo(slotId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.oppoGetSimPhonebookAllSpace();
        }
        return -1;
    }

    public int colorGetSimPhonebookUsedSpace(int slotId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManagerOppo(slotId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.oppoGetSimPhonebookUsedSpace();
        }
        return -1;
    }

    public boolean colorISPhoneBookReady(int slotId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManagerOppo(slotId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.isPhoneBookReady();
        }
        return false;
    }

    public boolean colorIsPhoneBookPbrExist(int slotId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManagerOppo(slotId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.isPhoneBookPbrExist();
        }
        return false;
    }

    public int oppoUpdateAdnRecordsWithContentValuesInEfBySearchUsingSubId(int subId, int efid, ContentValues values, String pin2) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.oppoUpdateAdnRecordsWithContentValuesInEfBySearch(efid, values, pin2);
        }
        Rlog.e(TAG, "oppoUpdateAdnRecordsWithContentValuesInEfBySearchUsingSubId iccPbkIntMgrProxy is null for Subscription:" + subId);
        return -1;
    }

    public int colorGetAdnEmailLenUsingSubId(int slotId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManagerOppo(slotId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.oppoGetAdnEmailLen();
        }
        return 30;
    }

    public int colorGetSimNameLenUsingSubId(int slotId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManagerOppo(slotId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.oppoGetSimPhonebookNameLength();
        }
        return 0;
    }

    public int colorAddAdnRecordsInEfBySearchExUsingSubId(int subId, int efid, String oldTag, String oldPhoneNumber, String newTag, String newPhoneNumber1, String newPhoneNumber2, String pin2, String email) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.oppoAddAdnRecordsInEfBySearchEx(efid, oldTag, oldPhoneNumber, newTag, newPhoneNumber1, newPhoneNumber2, pin2, email);
        }
        return 0;
    }

    public boolean colorUpdateAdnRecordsInEfByIndexExUsingSubId(int subId, int efid, String newTag, String newPhoneNumber1, String newPhoneNumber2, int index, String pin2, String email) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.oppoUpdateAdnRecordsInEfByIndexEx(efid, newTag, newPhoneNumber1, newPhoneNumber2, index, pin2, email);
        }
        return false;
    }

    public AdnRecord oppoGetAndRecordByIndexUsingSubId(int subId, int efid, int index) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.oppoGetAndRecordByIndex(efid, index);
        }
        return null;
    }
}
