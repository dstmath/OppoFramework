package com.android.server.connectivity.networkrecovery;

import android.util.Log;
import com.android.server.connectivity.networkrecovery.dnsresolve.Message;
import com.android.server.connectivity.networkrecovery.dnsresolve.Record;
import com.android.server.connectivity.networkrecovery.dnsresolve.Resolver;
import com.android.server.connectivity.networkrecovery.dnsresolve.Type;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class DnsDetector {
    private static final boolean DBG = true;
    private static final int DNS_QUERY_TIMEOUT = 10000;
    private static final int MAX_HOST_NAME = 4;
    private static final String TAG = "OPPODnsSelfrecoveryEngine.DnsDetector";
    private long mAvegDnsRtt = 0;
    private InetAddress mDns = null;
    private int mDnsQueryCount = 2;
    private int mDnsQueryTimeout = 2000;
    /* access modifiers changed from: private */
    public Resolver mDnsResolver;
    private String[] mHostNames = null;
    /* access modifiers changed from: private */
    public boolean mIsAvaible = false;
    private Collection<DnsDectorResult> mResults = new ArrayList();
    private int mSuccessRate = 0;

    DnsDetector(InetAddress dns, String[] hostnames, int dnsQueryTimeout, int dnsQueryCount) throws Exception {
        this.mDns = dns;
        this.mHostNames = hostnames;
        this.mDnsQueryTimeout = dnsQueryTimeout;
        this.mDnsQueryCount = dnsQueryCount;
        this.mIsAvaible = startDetector();
    }

    public class DnsDectorResult {
        public long mDnsRtt = 0;
        public String mHostname;
        public int mIpCount = 0;
        public boolean mSuccess = false;

        DnsDectorResult(String hostname) {
            this.mHostname = hostname;
        }

        /* access modifiers changed from: package-private */
        public void setDnsRtt(long rttTime) {
            this.mDnsRtt = rttTime;
        }

        /* access modifiers changed from: package-private */
        public long getDnsRtt() {
            return this.mDnsRtt;
        }

        /* access modifiers changed from: package-private */
        public String getHostname() {
            return this.mHostname;
        }

        /* access modifiers changed from: package-private */
        public boolean getResult() {
            return this.mSuccess;
        }

        /* access modifiers changed from: package-private */
        public void setResult(boolean isSuccess) {
            this.mSuccess = isSuccess;
        }

        /* access modifiers changed from: package-private */
        public void setIpCount(int count) {
            this.mIpCount = count;
        }

        /* access modifiers changed from: package-private */
        public int getIpCount() {
            return this.mIpCount;
        }
    }

    public boolean isAvaible() {
        return this.mIsAvaible;
    }

    public Collection<DnsDectorResult> getDnsResult() {
        return this.mResults;
    }

    public long getDnsAvegRtt() {
        return this.mAvegDnsRtt;
    }

    public int getResolveSuccessRate() {
        return this.mSuccessRate;
    }

    private void setDnsAvegRtt(long avegRtt) {
        this.mAvegDnsRtt = avegRtt;
    }

    private boolean startDetector() throws Exception {
        this.mIsAvaible = false;
        this.mDnsResolver = new Resolver(this.mDns, this.mDnsQueryTimeout, this.mDnsQueryCount);
        this.mDnsResolver.setUseUdp(true);
        int tmpsize = this.mHostNames.length;
        if (tmpsize > 4) {
            tmpsize = 4;
        }
        CountDownLatch latch = new CountDownLatch(tmpsize);
        AnonymousClass1DnsQueryThread[] queryThread = new AnonymousClass1DnsQueryThread[tmpsize];
        for (int count = 0; count < tmpsize; count++) {
            queryThread[count] = new Thread(this.mHostNames[count], latch) {
                /* class com.android.server.connectivity.networkrecovery.DnsDetector.AnonymousClass1DnsQueryThread */
                private String mHostname;
                private DnsDectorResult mResult;
                final /* synthetic */ CountDownLatch val$latch;

                /* Incorrect method signature, types: com.android.server.connectivity.networkrecovery.DnsDetector, java.lang.String */
                {
                    this.val$latch = r3;
                    this.mHostname = hostname;
                }

                public DnsDectorResult getResult() {
                    return this.mResult;
                }

                /* JADX WARNING: Removed duplicated region for block: B:12:0x0061  */
                /* JADX WARNING: Removed duplicated region for block: B:25:0x00af A[EDGE_INSN: B:25:0x00af->B:17:0x00af ?: BREAK  , SYNTHETIC] */
                public void run() {
                    try {
                        this.mResult = new DnsDectorResult(this.mHostname);
                        long startingTime = System.currentTimeMillis();
                        try {
                            Message reply = DnsDetector.this.mDnsResolver.request(this.mHostname, Type.A);
                            this.mResult.setDnsRtt(System.currentTimeMillis() - startingTime);
                            if (!reply.avivableflag) {
                                DnsDetector dnsDetector = DnsDetector.this;
                                dnsDetector.logd("not aviable,beacause the " + this.mHostname + " not avaivable.\n");
                                this.val$latch.countDown();
                                return;
                            }
                            Iterator<Record> it = reply.getAnswers().iterator();
                            while (true) {
                                if (!it.hasNext()) {
                                    Record a = it.next();
                                    Type recordType = a.getRecordType();
                                    DnsDetector dnsDetector2 = DnsDetector.this;
                                    dnsDetector2.logd("hostname = " + this.mHostname + "record type = " + recordType + " \n");
                                    if (a.getRecordType() == Type.A || Type.CNAME == a.getRecordType()) {
                                        boolean unused = DnsDetector.this.mIsAvaible = true;
                                        this.mResult.setResult(true);
                                    }
                                    if (!it.hasNext()) {
                                        break;
                                    }
                                }
                            }
                            this.val$latch.countDown();
                        } catch (IOException e) {
                            DnsDetector.this.logd("catch IOException\n");
                            this.val$latch.countDown();
                        }
                    } catch (Exception e2) {
                        DnsDetector.this.logd("catch Exception\n");
                        this.val$latch.countDown();
                    }
                }
            };
            queryThread[count].start();
        }
        try {
            latch.await(10000, TimeUnit.MILLISECONDS);
            for (int count2 = 0; count2 < tmpsize; count2++) {
                this.mResults.add(queryThread[count2].getResult());
            }
            dealResult();
            return this.mIsAvaible;
        } catch (InterruptedException e) {
            logd("Error: probe wait interrupted!");
            return false;
        }
    }

    private void dealResult() {
        long totalRtt = 0;
        int count = 0;
        int totalCount = 0;
        for (DnsDectorResult avgResult : this.mResults) {
            totalCount++;
            if (avgResult.getResult()) {
                count++;
                totalRtt += avgResult.getDnsRtt();
            }
        }
        if (count > 0) {
            this.mSuccessRate = (count * 100) / totalCount;
            this.mAvegDnsRtt = totalRtt / ((long) count);
            logd("mAvegDnsRtt " + this.mAvegDnsRtt + "ms.\n");
        }
    }

    /* access modifiers changed from: private */
    public void logd(String message) {
        Log.d(TAG, message);
    }
}
