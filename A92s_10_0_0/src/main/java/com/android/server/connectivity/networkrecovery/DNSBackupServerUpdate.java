package com.android.server.connectivity.networkrecovery;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.IDnsResolver;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiRomUpdateHelper;
import android.os.INetworkManagementService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.ServiceSpecificException;
import android.util.Log;
import com.android.server.connectivity.DnsManager;
import com.android.server.connectivity.NetworkAgentInfo;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class DNSBackupServerUpdate implements Runnable {
    private static final boolean DBG = true;
    private static final int DNS_SERVER_ENOUGH = 2;
    private static final int MAX_DNS_SERVERS = 4;
    private static final int MAX_DNS_SIZE = 4;
    private static final int MAX_IPV6_DNS_SERVERS = 2;
    private static final String TAG = "OPPODnsSelfrecoveryEngine.DNSBackupServerUpdate";
    private static final int TRIGER_TYPE_UPDATE_DNS = 3;
    private static final int TRIGER_TYPE_WIFI_CONNTED = 1;
    private static final int TRIGER_TYPE_WIFI_INTERNET_DETECTED_INVAILED = 2;
    public static boolean mIsDetecting = false;
    private final int DEFAULT_DNS_QUERY_RETRY_COUNT = 2;
    private final int DEFAULT_DNS_QUERY_TIMEOUT = 2000;
    private Collection<InetAddress> mAllocatedDnses;
    private Collection<InetAddress> mAvaibleDnses = new ArrayList();
    private Collection<InetAddress> mBackupDnses;
    private Context mContext = null;
    private final DnsManager mDnsManager;
    private int mDnsQueryRetryCount = 2;
    private int mDnsQueryTimeout = 2000;
    private List<DNSQueryResult> mDnsResultList = new ArrayList();
    private String[] mHostnames = null;
    private Collection<InetAddress> mLastDnses = new ArrayList();
    private NetworkAgentInfo mNai;
    private INetworkManagementService mNetd;
    private LinkProperties mNewLp;
    private DnsStatisticsResult mStatsticsResult = null;
    private int mTrigerType;
    private Collection<InetAddress> mUnAvaibleDnses = new ArrayList();
    private WifiRomUpdateHelper mWifiRomUpdateHelper = null;

    public DNSBackupServerUpdate(NetworkAgentInfo nai, INetworkManagementService netManager, Context context, Collection<InetAddress> backupDnses, String[] hostnames, int trigerType, DnsStatisticsResult result, DnsManager dnsManager) {
        this.mContext = context;
        this.mNetd = netManager;
        this.mNai = nai;
        this.mDnsManager = dnsManager;
        this.mHostnames = hostnames;
        this.mBackupDnses = backupDnses;
        this.mTrigerType = trigerType;
        logd("DNSBackupServerUpdate type = " + this.mTrigerType + StringUtils.LF);
        this.mStatsticsResult = result;
        this.mWifiRomUpdateHelper = WifiRomUpdateHelper.getInstance(this.mContext);
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            wifiRomUpdateHelper.enableVerboseLogging(0);
        }
        WifiRomUpdateHelper wifiRomUpdateHelper2 = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper2 != null) {
            this.mDnsQueryTimeout = wifiRomUpdateHelper2.getIntegerValue("NETWORK_DNS_QUERY_TIMEOUT", 2000).intValue();
            this.mDnsQueryRetryCount = this.mWifiRomUpdateHelper.getIntegerValue("NETWORK_DNS_QUERY_RETRY_COUNT", 2).intValue();
            return;
        }
        this.mDnsQueryTimeout = 2000;
        this.mDnsQueryRetryCount = 2;
    }

    public void updateNetworkInfo(NetworkAgentInfo nai, INetworkManagementService netManager, Context context, Collection<InetAddress> backupDnses, String[] hostnames, int trigerType, DnsStatisticsResult result) {
        this.mContext = context;
        this.mNetd = netManager;
        this.mNai = nai;
        this.mHostnames = hostnames;
        this.mBackupDnses = backupDnses;
        this.mTrigerType = trigerType;
        this.mStatsticsResult = result;
        this.mWifiRomUpdateHelper = WifiRomUpdateHelper.getInstance(this.mContext);
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            wifiRomUpdateHelper.enableVerboseLogging(0);
        }
        WifiRomUpdateHelper wifiRomUpdateHelper2 = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper2 != null) {
            this.mDnsQueryTimeout = wifiRomUpdateHelper2.getIntegerValue("NETWORK_DNS_QUERY_TIMEOUT", 2000).intValue();
            this.mDnsQueryRetryCount = this.mWifiRomUpdateHelper.getIntegerValue("NETWORK_DNS_QUERY_RETRY_COUNT", 2).intValue();
            return;
        }
        this.mDnsQueryTimeout = 2000;
        this.mDnsQueryRetryCount = 2;
    }

    private void updateDnsEngineStatistics(Collection<InetAddress> unAvaibleDnses) {
        this.mStatsticsResult.mDnsSelfRecoveryTotalCount++;
        int i = this.mTrigerType;
        if (i == 1) {
            this.mStatsticsResult.mDnsSelfRecoveryConnectedCount++;
        } else if (i == 2) {
            this.mStatsticsResult.mDnsSelfRecoveryInternetInvailedCount++;
        } else if (i == 3) {
            this.mStatsticsResult.mDnsSelfRecoveryDnsupdateCount++;
        }
        if (unAvaibleDnses != null) {
            int unAvaibleDnsSize = unAvaibleDnses.size();
            if (unAvaibleDnsSize > 4) {
                unAvaibleDnsSize = 4;
            }
            if (1 == unAvaibleDnsSize) {
                this.mStatsticsResult.mFristDnsInvailed++;
            } else if (2 == unAvaibleDnsSize) {
                this.mStatsticsResult.mDoubleDnsInvailed++;
            } else if (3 == unAvaibleDnsSize) {
                this.mStatsticsResult.mTripleDnsInvailed++;
            } else if (4 == unAvaibleDnsSize) {
                this.mStatsticsResult.mQuadraDnsInvailed++;
            }
        }
    }

    private void cleanAllDnsCache() {
        NetworkAgentInfo networkAgentInfo = this.mNai;
        if (networkAgentInfo != null && networkAgentInfo.network != null) {
            InetAddress.clearDnsCache();
            IDnsResolver dnsresolver = IDnsResolver.Stub.asInterface(ServiceManager.getService("dnsresolver"));
            if (dnsresolver != null) {
                try {
                    dnsresolver.resolveFlushCacheForNet(this.mNai.network.netId);
                } catch (RemoteException | ServiceSpecificException e) {
                    Log.d(TAG, "Exception destroying network: ");
                }
            }
        }
    }

    private boolean updateBackupDnsServer(int type) {
        int avaibleIPv4DNS = 0;
        int avaibleIPv6DNS = 0;
        int dnsServerCount = 0;
        boolean allDnsServerAvailable = false;
        getCurrentDnsServer(type);
        List<DNSQueryResult> list = this.mDnsResultList;
        if (list != null) {
            list.clear();
        } else {
            this.mDnsResultList = new ArrayList();
        }
        Collection<InetAddress> collection = this.mAvaibleDnses;
        if (collection != null) {
            collection.clear();
        } else {
            this.mAvaibleDnses = new ArrayList();
        }
        Collection<InetAddress> collection2 = this.mUnAvaibleDnses;
        if (collection2 != null) {
            collection2.clear();
        } else {
            this.mUnAvaibleDnses = new ArrayList();
        }
        try {
            Iterator<InetAddress> it = this.mAllocatedDnses.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                InetAddress dnsAddress = it.next();
                if (dnsServerCount >= 4) {
                    logd("next dns server is not in the system cache\n");
                    break;
                }
                dnsServerCount++;
                if (dnsAddress instanceof Inet6Address) {
                    avaibleIPv6DNS++;
                    if (avaibleIPv6DNS <= 2) {
                        this.mAvaibleDnses.add(dnsAddress);
                    }
                } else if (isDNSServerAvaiable(dnsAddress, this.mHostnames)) {
                    avaibleIPv4DNS++;
                    this.mAvaibleDnses.add(dnsAddress);
                } else {
                    this.mUnAvaibleDnses.add(dnsAddress);
                }
            }
            if (this.mUnAvaibleDnses.size() == 0 && avaibleIPv4DNS > 0) {
                allDnsServerAvailable = true;
            }
            if (avaibleIPv4DNS == 0) {
                for (InetAddress backupDns : this.mBackupDnses) {
                    if (!this.mAvaibleDnses.contains(backupDns)) {
                        if (isDNSServerAvaiable(backupDns, this.mHostnames)) {
                            avaibleIPv4DNS++;
                            this.mAvaibleDnses.add(backupDns);
                            if (this.mAvaibleDnses.size() > 2) {
                                break;
                            }
                        } else {
                            continue;
                        }
                    }
                }
            }
            if (avaibleIPv4DNS <= 0 || allDnsServerAvailable) {
                logd("no avivable dns server or all dns server aviable, do nothing.\n");
                updateDnsEngineStatistics(null);
                printCurrentDnsServers(this.mAllocatedDnses);
                return true;
            }
            replaceAllDNSes(this.mAvaibleDnses, type);
            return true;
        } catch (Exception e) {
            logd("updateBackupDnsServer " + e);
            return false;
        }
    }

    private boolean isOriginalNetworkConnected(int type) {
        NetworkInfo networkInfo = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getNetworkInfo(type);
        if (networkInfo != null) {
            logd("networkInfo.DetailedState = " + networkInfo.getDetailedState() + StringUtils.LF);
            return networkInfo.isConnected();
        }
        logd("networkInfo.DetailedState NULL\n");
        return false;
    }

    private boolean getCurrentDnsServer(int type) {
        this.mNewLp = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getLinkProperties(type);
        LinkProperties linkProperties = this.mNewLp;
        if (linkProperties != null) {
            Collection<InetAddress> dnses = linkProperties.getDnsServers();
            Collection<InetAddress> collection = this.mAllocatedDnses;
            if (collection != null) {
                collection.clear();
            }
            this.mAllocatedDnses = new ArrayList();
            for (InetAddress ia : dnses) {
                this.mAllocatedDnses.add(ia);
            }
            return true;
        }
        logd("newLp NULL\n");
        return false;
    }

    private boolean isDNSServerAvaiable(InetAddress dns, String[] hostnames) throws Exception {
        DnsDetector dnsdetector = new DnsDetector(dns, hostnames, this.mDnsQueryTimeout, this.mDnsQueryRetryCount);
        if (dnsdetector.isAvaible()) {
            this.mDnsResultList.add(new DNSQueryResult(dnsdetector.getDnsAvegRtt(), dns, dnsdetector.isAvaible(), dnsdetector.getResolveSuccessRate()));
        }
        return dnsdetector.isAvaible();
    }

    private boolean replaceAllDNSes(Collection<InetAddress> avaibleReplaceDnses, int type) {
        LinkProperties newLp = new LinkProperties(this.mNai.linkProperties);
        Network nw = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getNetworkForType(type);
        if (nw == null) {
            return false;
        }
        int netId = nw.netId;
        this.mLastDnses.clear();
        for (InetAddress backupDns : avaibleReplaceDnses) {
            this.mLastDnses.add(backupDns);
            logd("available dns:" + backupDns + StringUtils.LF);
        }
        logd("mNetd.setDnsConfigurationForNetwork\n");
        updateDnsEngineStatistics(this.mUnAvaibleDnses);
        NetworkAgentInfo networkAgentInfo = this.mNai;
        if (networkAgentInfo == null || networkAgentInfo.linkProperties == null) {
            logd("setDnsServers: error\n");
            return false;
        }
        try {
            newLp.setDnsServers(avaibleReplaceDnses);
            try {
                this.mDnsManager.setDnsConfigurationForNetwork(netId, newLp, false);
                printCurrentDnsServers(avaibleReplaceDnses);
                cleanAllDnsCache();
                return true;
            } catch (Exception e) {
                logd("Exception in setDnsConfigurationForNetwork: " + e);
                return false;
            }
        } catch (Exception e2) {
            logd("setDnsServers: error" + e2);
            return false;
        }
    }

    private void printCurrentDnsServers(Collection<InetAddress> dnses) {
        StringBuilder sb = new StringBuilder();
        sb.append("CURRENT_DNS_SERVER:");
        Iterator<InetAddress> it = dnses.iterator();
        while (it.hasNext()) {
            sb.append(it.next().toString() + "--");
        }
        logd(sb.toString());
    }

    public void run() {
        try {
            mIsDetecting = true;
            logd("DNSBackupServerUpdate run type = " + this.mTrigerType + StringUtils.LF);
            if (isOriginalNetworkConnected(1)) {
                logd("network avaiable,start dnsdetetor\n");
                updateBackupDnsServer(1);
            } else {
                logd("network not avaiable,do not start dnsdetetor\n");
            }
            mIsDetecting = false;
        } catch (Exception e) {
            logd("Exception in setDnsConfigurationForNetwork: " + e);
            mIsDetecting = false;
        }
    }

    private void logd(String message) {
        Log.d(TAG, message);
    }
}
