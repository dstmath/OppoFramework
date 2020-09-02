package com.mediatek.internal.telephony.dataconnection;

import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.util.SparseArray;
import com.android.internal.telephony.Phone;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;

public class DcFailCauseManager {
    private static final int[] CC33_FAIL_CAUSE_TABLE = {29, 33};
    public static final boolean DBG = true;
    public static final String DEFAULT_DATA_RETRY_CONFIG_OP19 = "max_retries=10, 720000,1440000,2880000,5760000,11520000,23040000,23040000,23040000,23040000,46080000";
    public static final String LOG_TAG = "DcFcMgr";
    public static final boolean VDBG = false;
    private static final SparseArray<DcFailCauseManager> sDcFailCauseManager = new SparseArray<>();
    private static final String[][] specificPLMN = {new String[]{"50501"}, new String[]{"732101"}};
    private Phone mPhone;

    public enum Operator {
        NONE(-1),
        OP19(0),
        OP120(1);
        
        private static final HashMap<Integer, Operator> lookup = new HashMap<>();
        private int value;

        static {
            Iterator it = EnumSet.allOf(Operator.class).iterator();
            while (it.hasNext()) {
                Operator op = (Operator) it.next();
                lookup.put(Integer.valueOf(op.getValue()), op);
            }
        }

        private Operator(int value2) {
            this.value = value2;
        }

        public int getValue() {
            return this.value;
        }

        public static Operator get(int value2) {
            return lookup.get(Integer.valueOf(value2));
        }
    }

    private enum retryConfigForDefault {
        maxRetryCount(1),
        retryTime(0),
        randomizationTime(0);
        
        private final int value;

        private retryConfigForDefault(int value2) {
            this.value = value2;
        }

        public int getValue() {
            return this.value;
        }
    }

    public enum retryConfigForCC33 {
        maxRetryCount(2),
        retryTime(45000),
        randomizationTime(0);
        
        private final int value;

        private retryConfigForCC33(int value2) {
            this.value = value2;
        }

        public int getValue() {
            return this.value;
        }
    }

    public static DcFailCauseManager getInstance(Phone phone) {
        if (phone != null) {
            int phoneId = phone.getPhoneId();
            if (phoneId < 0) {
                Rlog.e(LOG_TAG, "PhoneId[" + phoneId + "] is invalid!");
                return null;
            }
            DcFailCauseManager dcFcMgr = sDcFailCauseManager.get(phoneId);
            if (dcFcMgr != null) {
                return dcFcMgr;
            }
            Rlog.d(LOG_TAG, "For phoneId:" + phoneId + " doesn't exist, create it");
            DcFailCauseManager dcFcMgr2 = new DcFailCauseManager(phone);
            sDcFailCauseManager.put(phoneId, dcFcMgr2);
            return dcFcMgr2;
        }
        Rlog.e(LOG_TAG, "Can't get phone to init!");
        return null;
    }

    private DcFailCauseManager(Phone phone) {
        log("DcFcMgr.constructor");
        this.mPhone = phone;
    }

    public void dispose() {
        log("DcFcMgr.dispose");
        sDcFailCauseManager.remove(this.mPhone.getPhoneId());
    }

    public long getSuggestedRetryDelayByOp(int cause) {
        long suggestedRetryTime = -2;
        if (AnonymousClass1.$SwitchMap$com$mediatek$internal$telephony$dataconnection$DcFailCauseManager$Operator[getSpecificNetworkOperator().ordinal()] == 1) {
            for (int tempCause : CC33_FAIL_CAUSE_TABLE) {
                if (cause == tempCause) {
                    suggestedRetryTime = (long) retryConfigForCC33.retryTime.getValue();
                }
            }
        }
        return suggestedRetryTime;
    }

    /* renamed from: com.mediatek.internal.telephony.dataconnection.DcFailCauseManager$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$mediatek$internal$telephony$dataconnection$DcFailCauseManager$Operator = new int[Operator.values().length];

        static {
            try {
                $SwitchMap$com$mediatek$internal$telephony$dataconnection$DcFailCauseManager$Operator[Operator.OP120.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$mediatek$internal$telephony$dataconnection$DcFailCauseManager$Operator[Operator.OP19.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    public long getRetryTimeByIndex(int idx, Operator op) {
        String configStr = null;
        if (AnonymousClass1.$SwitchMap$com$mediatek$internal$telephony$dataconnection$DcFailCauseManager$Operator[op.ordinal()] == 2) {
            configStr = DEFAULT_DATA_RETRY_CONFIG_OP19;
        }
        if (configStr == null) {
            return -1;
        }
        try {
            return Long.parseLong(configStr.split(",")[idx]);
        } catch (IndexOutOfBoundsException e) {
            loge("get retry time by index fail");
            e.printStackTrace();
            return -1;
        }
    }

    public boolean isSpecificNetworkAndSimOperator(Operator op) {
        if (op == null) {
            loge("op is null, return false!");
            return false;
        }
        Operator networkOp = getSpecificNetworkOperator();
        Operator simOp = getSpecificSimOperator();
        if (op == networkOp && op == simOp) {
            return true;
        }
        return false;
    }

    public boolean isSpecificNetworkOperator(Operator op) {
        if (op == null) {
            loge("op is null, return false!");
            return false;
        } else if (op == getSpecificNetworkOperator()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isNetworkOperatorForCC33() {
        if (AnonymousClass1.$SwitchMap$com$mediatek$internal$telephony$dataconnection$DcFailCauseManager$Operator[getSpecificNetworkOperator().ordinal()] != 1) {
            return false;
        }
        return true;
    }

    private Operator getSpecificNetworkOperator() {
        Operator operator = Operator.NONE;
        String plmn = "";
        try {
            plmn = TelephonyManager.getDefault().getNetworkOperatorForPhone(this.mPhone.getPhoneId());
            log("Check PLMN=" + plmn);
        } catch (Exception e) {
            loge("get plmn fail");
            e.printStackTrace();
        }
        for (int i = 0; i < specificPLMN.length; i++) {
            boolean isServingInSpecificPlmn = false;
            int j = 0;
            while (true) {
                String[][] strArr = specificPLMN;
                if (j >= strArr[i].length) {
                    break;
                } else if (plmn.equals(strArr[i][j])) {
                    isServingInSpecificPlmn = true;
                    break;
                } else {
                    j++;
                }
            }
            if (isServingInSpecificPlmn) {
                Operator operator2 = Operator.get(i);
                log("Serving in specific network op=" + operator2 + "(" + i + ")");
                return operator2;
            }
        }
        return operator;
    }

    private Operator getSpecificSimOperator() {
        Operator operator = Operator.NONE;
        String hplmn = "";
        try {
            hplmn = TelephonyManager.getDefault().getSimOperatorNumericForPhone(this.mPhone.getPhoneId());
            log("Check HPLMN=" + hplmn);
        } catch (Exception e) {
            loge("get hplmn fail");
            e.printStackTrace();
        }
        for (int i = 0; i < specificPLMN.length; i++) {
            boolean isServingInSpecificPlmn = false;
            int j = 0;
            while (true) {
                String[][] strArr = specificPLMN;
                if (j >= strArr[i].length) {
                    break;
                } else if (hplmn.equals(strArr[i][j])) {
                    isServingInSpecificPlmn = true;
                    break;
                } else {
                    j++;
                }
            }
            if (isServingInSpecificPlmn) {
                Operator operator2 = Operator.get(i);
                log("Serving in specific sim op=" + operator2 + "(" + i + ")");
                return operator2;
            }
        }
        return operator;
    }

    public String toString() {
        return "sDcFailCauseManager: " + sDcFailCauseManager;
    }

    private void loge(String s) {
        Rlog.e(LOG_TAG, s);
    }

    private void log(String s) {
        Rlog.d(LOG_TAG, s);
    }
}
