package cm.android.mdm;

import android.content.Context;
import cm.android.mdm.interfaces.IApplicationManager;
import cm.android.mdm.interfaces.IBrowserManager;
import cm.android.mdm.interfaces.IContactsManager;
import cm.android.mdm.interfaces.IDeviceManager;
import cm.android.mdm.interfaces.INetworkManager;
import cm.android.mdm.interfaces.IPackageManager;
import cm.android.mdm.interfaces.IPhoneManager;
import cm.android.mdm.interfaces.IRecordManager;
import cm.android.mdm.interfaces.IRestrictionManager;
import cm.android.mdm.manager.ApplicationManager;
import cm.android.mdm.manager.BrowserManager;
import cm.android.mdm.manager.ContactsManager;
import cm.android.mdm.manager.DeviceManager;
import cm.android.mdm.manager.NetworkManager;
import cm.android.mdm.manager.PackageManager2;
import cm.android.mdm.manager.PhoneManager;
import cm.android.mdm.manager.RecordManager;
import cm.android.mdm.manager.RestrictionManager;

public class MdmAgent {
    private static MdmAgent mMdmAgent = null;
    private ApplicationManager mApplicationManager;
    private BrowserManager mBrowserManager;
    private ContactsManager mContactsManager;
    private Context mContext = null;
    private DeviceManager mDeviceManager;
    private NetworkManager mNetworkManager;
    private PackageManager2 mPackageManager2;
    private PhoneManager mPhoneManager;
    private RecordManager mRecordManager;
    private RestrictionManager mRestrictionManager;

    public static MdmAgent getInstance(Context context) {
        if (mMdmAgent == null) {
            mMdmAgent = new MdmAgent(context);
        }
        return mMdmAgent;
    }

    public MdmAgent(Context context) {
        this.mContext = context;
    }

    public IApplicationManager getApplicationManager() {
        if (this.mApplicationManager == null) {
            this.mApplicationManager = new ApplicationManager(this.mContext);
        }
        return this.mApplicationManager;
    }

    public IBrowserManager getBrowserManager() {
        if (this.mBrowserManager == null) {
            this.mBrowserManager = new BrowserManager(this.mContext);
        }
        return this.mBrowserManager;
    }

    public IContactsManager getContactsManager() {
        if (this.mContactsManager == null) {
            this.mContactsManager = new ContactsManager(this.mContext);
        }
        return this.mContactsManager;
    }

    public IDeviceManager getDeviceManager() {
        if (this.mDeviceManager == null) {
            this.mDeviceManager = new DeviceManager(this.mContext);
        }
        return this.mDeviceManager;
    }

    public INetworkManager getNetworkManager() {
        if (this.mNetworkManager == null) {
            this.mNetworkManager = new NetworkManager(this.mContext);
        }
        return this.mNetworkManager;
    }

    public IPackageManager getPackageManager() {
        if (this.mPackageManager2 == null) {
            this.mPackageManager2 = new PackageManager2(this.mContext);
        }
        return this.mPackageManager2;
    }

    public IPhoneManager getPhoneManager() {
        if (this.mPhoneManager == null) {
            this.mPhoneManager = new PhoneManager(this.mContext);
        }
        return this.mPhoneManager;
    }

    public IRestrictionManager getRestrictionManager() {
        if (this.mRestrictionManager == null) {
            this.mRestrictionManager = new RestrictionManager(this.mContext);
        }
        return this.mRestrictionManager;
    }

    public IRecordManager getRecordManager() {
        if (this.mRecordManager == null) {
            this.mRecordManager = new RecordManager(this.mContext);
        }
        return this.mRecordManager;
    }
}
