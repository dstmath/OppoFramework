package com.mediatek.internal.telephony.phb;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.Rlog;
import com.android.internal.telephony.IccPhoneBookInterfaceManager;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.SubscriptionController;
import com.mediatek.internal.telephony.phb.IMtkIccPhoneBook;
import java.util.List;

public class MtkUiccPhoneBookController extends IMtkIccPhoneBook.Stub {
    private static final String TAG = "MtkUiccPhoneBookController";
    private Phone[] mPhone;

    public MtkUiccPhoneBookController(Phone[] phone) {
        if (ServiceManager.getService("mtksimphonebook") == null) {
            ServiceManager.addService("mtksimphonebook", this);
        }
        this.mPhone = phone;
    }

    @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
    public List<MtkAdnRecord> getAdnRecordsInEf(int efid) throws RemoteException {
        return getAdnRecordsInEfForSubscriber(getDefaultSubscription(), efid);
    }

    @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
    public List<MtkAdnRecord> getAdnRecordsInEfForSubscriber(int subId, int efid) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return ((MtkIccPhoneBookInterfaceManager) iccPbkIntMgr).getAdnRecordsInEf(efid, null);
        }
        Rlog.e(TAG, "getAdnRecordsInEf iccPbkIntMgr isnull for Subscription:" + subId);
        return null;
    }

    @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
    public int updateAdnRecordsInEfBySearchWithError(int subId, int efid, String oldTag, String oldPhoneNumber, String newTag, String newPhoneNumber, String pin2) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return ((MtkIccPhoneBookInterfaceManager) iccPbkIntMgr).updateAdnRecordsInEfBySearchWithError(efid, oldTag, oldPhoneNumber, newTag, newPhoneNumber, pin2);
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return 0;
    }

    @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
    public int updateUsimPBRecordsInEfBySearchWithError(int subId, int efid, String oldTag, String oldPhoneNumber, String oldAnr, String oldGrpIds, String[] oldEmails, String newTag, String newPhoneNumber, String newAnr, String newGrpIds, String[] newEmails) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return ((MtkIccPhoneBookInterfaceManager) iccPbkIntMgr).updateUsimPBRecordsInEfBySearchWithError(efid, oldTag, oldPhoneNumber, oldAnr, oldGrpIds, oldEmails, newTag, newPhoneNumber, newAnr, newGrpIds, newEmails);
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return 0;
    }

    @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
    public int updateAdnRecordsInEfByIndexWithError(int subId, int efid, String newTag, String newPhoneNumber, int index, String pin2) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return ((MtkIccPhoneBookInterfaceManager) iccPbkIntMgr).updateAdnRecordsInEfByIndexWithError(efid, newTag, newPhoneNumber, index, pin2);
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return 0;
    }

    @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
    public int updateUsimPBRecordsInEfByIndexWithError(int subId, int efid, String newTag, String newPhoneNumber, String newAnr, String newGrpIds, String[] newEmails, int index) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return ((MtkIccPhoneBookInterfaceManager) iccPbkIntMgr).updateUsimPBRecordsInEfByIndexWithError(efid, newTag, newPhoneNumber, newAnr, newGrpIds, newEmails, index);
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return 0;
    }

    @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
    public int updateUsimPBRecordsByIndexWithError(int subId, int efid, MtkAdnRecord record, int index) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return ((MtkIccPhoneBookInterfaceManager) iccPbkIntMgr).updateUsimPBRecordsByIndexWithError(efid, record, index);
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return 0;
    }

    @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
    public int updateUsimPBRecordsBySearchWithError(int subId, int efid, MtkAdnRecord oldAdn, MtkAdnRecord newAdn) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return ((MtkIccPhoneBookInterfaceManager) iccPbkIntMgr).updateUsimPBRecordsBySearchWithError(efid, oldAdn, newAdn);
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return 0;
    }

    @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
    public boolean isPhbReady(int subId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return ((MtkIccPhoneBookInterfaceManager) iccPbkIntMgr).isPhbReady();
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return false;
    }

    @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
    public List<UsimGroup> getUsimGroups(int subId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return ((MtkIccPhoneBookInterfaceManager) iccPbkIntMgr).getUsimGroups();
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return null;
    }

    @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
    public String getUsimGroupById(int subId, int nGasId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return ((MtkIccPhoneBookInterfaceManager) iccPbkIntMgr).getUsimGroupById(nGasId);
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return null;
    }

    @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
    public boolean removeUsimGroupById(int subId, int nGasId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return ((MtkIccPhoneBookInterfaceManager) iccPbkIntMgr).removeUsimGroupById(nGasId);
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return false;
    }

    @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
    public int insertUsimGroup(int subId, String grpName) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return ((MtkIccPhoneBookInterfaceManager) iccPbkIntMgr).insertUsimGroup(grpName);
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return -1;
    }

    @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
    public int updateUsimGroup(int subId, int nGasId, String grpName) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return ((MtkIccPhoneBookInterfaceManager) iccPbkIntMgr).updateUsimGroup(nGasId, grpName);
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return -1;
    }

    @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
    public boolean addContactToGroup(int subId, int adnIndex, int grpIndex) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return ((MtkIccPhoneBookInterfaceManager) iccPbkIntMgr).addContactToGroup(adnIndex, grpIndex);
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return false;
    }

    @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
    public boolean removeContactFromGroup(int subId, int adnIndex, int grpIndex) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return ((MtkIccPhoneBookInterfaceManager) iccPbkIntMgr).removeContactFromGroup(adnIndex, grpIndex);
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return false;
    }

    @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
    public boolean updateContactToGroups(int subId, int adnIndex, int[] grpIdList) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return ((MtkIccPhoneBookInterfaceManager) iccPbkIntMgr).updateContactToGroups(adnIndex, grpIdList);
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return false;
    }

    @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
    public boolean moveContactFromGroupsToGroups(int subId, int adnIndex, int[] fromGrpIdList, int[] toGrpIdList) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return ((MtkIccPhoneBookInterfaceManager) iccPbkIntMgr).moveContactFromGroupsToGroups(adnIndex, fromGrpIdList, toGrpIdList);
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return false;
    }

    @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
    public int hasExistGroup(int subId, String grpName) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return ((MtkIccPhoneBookInterfaceManager) iccPbkIntMgr).hasExistGroup(grpName);
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return -1;
    }

    @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
    public int getUsimGrpMaxNameLen(int subId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return ((MtkIccPhoneBookInterfaceManager) iccPbkIntMgr).getUsimGrpMaxNameLen();
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return -1;
    }

    @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
    public int getUsimGrpMaxCount(int subId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return ((MtkIccPhoneBookInterfaceManager) iccPbkIntMgr).getUsimGrpMaxCount();
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return -1;
    }

    @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
    public List<AlphaTag> getUsimAasList(int subId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return ((MtkIccPhoneBookInterfaceManager) iccPbkIntMgr).getUsimAasList();
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return null;
    }

    @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
    public String getUsimAasById(int subId, int index) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return ((MtkIccPhoneBookInterfaceManager) iccPbkIntMgr).getUsimAasById(index);
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return null;
    }

    @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
    public int insertUsimAas(int subId, String aasName) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return ((MtkIccPhoneBookInterfaceManager) iccPbkIntMgr).insertUsimAas(aasName);
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return 0;
    }

    @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
    public int getAnrCount(int subId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return ((MtkIccPhoneBookInterfaceManager) iccPbkIntMgr).getAnrCount();
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return 0;
    }

    @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
    public int getEmailCount(int subId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return ((MtkIccPhoneBookInterfaceManager) iccPbkIntMgr).getEmailCount();
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return 0;
    }

    @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
    public int getUsimAasMaxCount(int subId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return ((MtkIccPhoneBookInterfaceManager) iccPbkIntMgr).getUsimAasMaxCount();
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return 0;
    }

    @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
    public int getUsimAasMaxNameLen(int subId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return ((MtkIccPhoneBookInterfaceManager) iccPbkIntMgr).getUsimAasMaxNameLen();
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return 0;
    }

    @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
    public boolean updateUsimAas(int subId, int index, int pbrIndex, String aasName) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return ((MtkIccPhoneBookInterfaceManager) iccPbkIntMgr).updateUsimAas(index, pbrIndex, aasName);
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return false;
    }

    @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
    public boolean removeUsimAasById(int subId, int index, int pbrIndex) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return ((MtkIccPhoneBookInterfaceManager) iccPbkIntMgr).removeUsimAasById(index, pbrIndex);
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return false;
    }

    @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
    public boolean hasSne(int subId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return ((MtkIccPhoneBookInterfaceManager) iccPbkIntMgr).hasSne();
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return false;
    }

    @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
    public int getSneRecordLen(int subId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return ((MtkIccPhoneBookInterfaceManager) iccPbkIntMgr).getSneRecordLen();
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return 0;
    }

    @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
    public boolean isAdnAccessible(int subId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return ((MtkIccPhoneBookInterfaceManager) iccPbkIntMgr).isAdnAccessible();
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return true;
    }

    @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
    public UsimPBMemInfo[] getPhonebookMemStorageExt(int subId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return ((MtkIccPhoneBookInterfaceManager) iccPbkIntMgr).getPhonebookMemStorageExt();
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return null;
    }

    @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
    public int getUpbDone(int subId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return ((MtkIccPhoneBookInterfaceManager) iccPbkIntMgr).getUpbDone();
        }
        Rlog.e(TAG, "getUpbDone iccPbkIntMgr is null for Subscription:" + subId);
        return -1;
    }

    @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
    public int[] getAdnRecordsCapacity() throws RemoteException {
        return getAdnRecordsCapacityForSubscriber(getDefaultSubscription());
    }

    @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
    public int[] getAdnRecordsCapacityForSubscriber(int subId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return ((MtkIccPhoneBookInterfaceManager) iccPbkIntMgr).getAdnRecordsCapacity();
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

    private MtkIccPhoneBookInterfaceManager getIccPhoneBookInterfaceManagerOppo(int slotId) {
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

    @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
    public int colorGetSimPhonebookAllSpace(int slotId) throws RemoteException {
        MtkIccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManagerOppo(slotId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.oppoGetSimPhonebookAllSpace();
        }
        return -1;
    }

    @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
    public int colorGetSimPhonebookUsedSpace(int slotId) throws RemoteException {
        MtkIccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManagerOppo(slotId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.oppoGetSimPhonebookUsedSpace();
        }
        return -1;
    }

    @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
    public boolean colorISPhoneBookReady(int slotId) throws RemoteException {
        MtkIccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManagerOppo(slotId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.isPhoneBookReady();
        }
        return false;
    }

    @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
    public boolean colorIsPhoneBookPbrExist(int slotId) throws RemoteException {
        MtkIccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManagerOppo(slotId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.isPhoneBookPbrExist();
        }
        return false;
    }

    @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
    public int colorGetAdnEmailLenUsingSubId(int slotId) throws RemoteException {
        MtkIccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManagerOppo(slotId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.oppoGetAdnEmailLen();
        }
        return 30;
    }

    @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
    public int colorGetSimNameLenUsingSubId(int slotId) throws RemoteException {
        MtkIccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManagerOppo(slotId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.oppoGetSimPhonebookNameLength();
        }
        return 0;
    }

    @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
    public int colorAddAdnRecordsInEfBySearchExUsingSubId(int subId, int efid, String oldTag, String oldPhoneNumber, String newTag, String newPhoneNumber1, String newPhoneNumber2, String pin2, String email) throws RemoteException {
        MtkIccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManagerOppo(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.oppoAddAdnRecordsInEfBySearchEx(efid, oldTag, oldPhoneNumber, newTag, newPhoneNumber1, newPhoneNumber2, pin2, email);
        }
        return 0;
    }

    @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
    public boolean colorUpdateAdnRecordsInEfByIndexExUsingSubId(int subId, int efid, String newTag, String newPhoneNumber1, String newPhoneNumber2, int index, String pin2, String email) throws RemoteException {
        MtkIccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManagerOppo(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.oppoUpdateAdnRecordsInEfByIndexEx(efid, newTag, newPhoneNumber1, newPhoneNumber2, index, pin2, email);
        }
        return false;
    }
}
