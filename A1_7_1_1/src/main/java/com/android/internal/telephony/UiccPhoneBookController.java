package com.android.internal.telephony;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.Rlog;
import com.android.internal.telephony.IIccPhoneBook.Stub;
import com.android.internal.telephony.uicc.AdnRecord;
import com.mediatek.internal.telephony.uicc.AlphaTag;
import com.mediatek.internal.telephony.uicc.UsimGroup;
import com.mediatek.internal.telephony.uicc.UsimPBMemInfo;
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

    public int updateAdnRecordsInEfBySearchWithError(int subId, int efid, String oldTag, String oldPhoneNumber, String newTag, String newPhoneNumber, String pin2) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.updateAdnRecordsInEfBySearchWithError(efid, oldTag, oldPhoneNumber, newTag, newPhoneNumber, pin2);
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return 0;
    }

    public int updateUsimPBRecordsInEfBySearchWithError(int subId, int efid, String oldTag, String oldPhoneNumber, String oldAnr, String oldGrpIds, String[] oldEmails, String newTag, String newPhoneNumber, String newAnr, String newGrpIds, String[] newEmails) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.updateUsimPBRecordsInEfBySearchWithError(efid, oldTag, oldPhoneNumber, oldAnr, oldGrpIds, oldEmails, newTag, newPhoneNumber, newAnr, newGrpIds, newEmails);
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return 0;
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

    public int updateAdnRecordsInEfByIndexWithError(int subId, int efid, String newTag, String newPhoneNumber, int index, String pin2) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.updateAdnRecordsInEfByIndexWithError(efid, newTag, newPhoneNumber, index, pin2);
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return 0;
    }

    public int updateUsimPBRecordsInEfByIndexWithError(int subId, int efid, String newTag, String newPhoneNumber, String newAnr, String newGrpIds, String[] newEmails, int index) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.updateUsimPBRecordsInEfByIndexWithError(efid, newTag, newPhoneNumber, newAnr, newGrpIds, newEmails, index);
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return 0;
    }

    public int updateUsimPBRecordsByIndexWithError(int subId, int efid, AdnRecord record, int index) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.updateUsimPBRecordsByIndexWithError(efid, record, index);
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return 0;
    }

    public int updateUsimPBRecordsBySearchWithError(int subId, int efid, AdnRecord oldAdn, AdnRecord newAdn) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.updateUsimPBRecordsBySearchWithError(efid, oldAdn, newAdn);
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return 0;
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

    public boolean isPhbReady(int subId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.isPhbReady();
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return false;
    }

    public List<UsimGroup> getUsimGroups(int subId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.getUsimGroups();
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return null;
    }

    public String getUsimGroupById(int subId, int nGasId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.getUsimGroupById(nGasId);
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return null;
    }

    public boolean removeUsimGroupById(int subId, int nGasId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.removeUsimGroupById(nGasId);
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return false;
    }

    public int insertUsimGroup(int subId, String grpName) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.insertUsimGroup(grpName);
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return -1;
    }

    public int updateUsimGroup(int subId, int nGasId, String grpName) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.updateUsimGroup(nGasId, grpName);
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return -1;
    }

    public boolean addContactToGroup(int subId, int adnIndex, int grpIndex) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.addContactToGroup(adnIndex, grpIndex);
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return false;
    }

    public boolean removeContactFromGroup(int subId, int adnIndex, int grpIndex) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.removeContactFromGroup(adnIndex, grpIndex);
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return false;
    }

    public boolean updateContactToGroups(int subId, int adnIndex, int[] grpIdList) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.updateContactToGroups(adnIndex, grpIdList);
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return false;
    }

    public boolean moveContactFromGroupsToGroups(int subId, int adnIndex, int[] fromGrpIdList, int[] toGrpIdList) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.moveContactFromGroupsToGroups(adnIndex, fromGrpIdList, toGrpIdList);
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return false;
    }

    public int hasExistGroup(int subId, String grpName) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.hasExistGroup(grpName);
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return -1;
    }

    public int getUsimGrpMaxNameLen(int subId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.getUsimGrpMaxNameLen();
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return -1;
    }

    public int getUsimGrpMaxCount(int subId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.getUsimGrpMaxCount();
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return -1;
    }

    public List<AlphaTag> getUsimAasList(int subId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.getUsimAasList();
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return null;
    }

    public String getUsimAasById(int subId, int index) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.getUsimAasById(index);
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return null;
    }

    public int insertUsimAas(int subId, String aasName) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.insertUsimAas(aasName);
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return 0;
    }

    public int getAnrCount(int subId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.getAnrCount();
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return 0;
    }

    public int getEmailCount(int subId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.getEmailCount();
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return 0;
    }

    public int getUsimAasMaxCount(int subId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.getUsimAasMaxCount();
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return 0;
    }

    public int getUsimAasMaxNameLen(int subId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.getUsimAasMaxNameLen();
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return 0;
    }

    public boolean updateUsimAas(int subId, int index, int pbrIndex, String aasName) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.updateUsimAas(index, pbrIndex, aasName);
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return false;
    }

    public boolean removeUsimAasById(int subId, int index, int pbrIndex) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.removeUsimAasById(index, pbrIndex);
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return false;
    }

    public boolean hasSne(int subId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.hasSne();
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return false;
    }

    public int getSneRecordLen(int subId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.getSneRecordLen();
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return 0;
    }

    public boolean isAdnAccessible(int subId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.isAdnAccessible();
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return true;
    }

    public UsimPBMemInfo[] getPhonebookMemStorageExt(int subId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.getPhonebookMemStorageExt();
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return null;
    }

    public int getUpbDone(int subId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.getUpbDone();
        }
        Rlog.e(TAG, "getUpbDone iccPbkIntMgr is null for Subscription:" + subId);
        return -1;
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
}
