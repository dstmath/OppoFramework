package mediatek.telecom;

import android.telephony.PhoneNumberUtils;
import java.util.IllegalFormatException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class FormattedLog {
    private String mLogString;

    public enum OpType {
        OPERATION,
        NOTIFY,
        DUMP
    }

    /* synthetic */ FormattedLog(String x0, Object[] x1, AnonymousClass1 x2) {
        this(x0, x1);
    }

    /* renamed from: mediatek.telecom.FormattedLog$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$mediatek$telecom$FormattedLog$OpType = new int[OpType.values().length];

        static {
            try {
                $SwitchMap$mediatek$telecom$FormattedLog$OpType[OpType.OPERATION.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$mediatek$telecom$FormattedLog$OpType[OpType.NOTIFY.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$mediatek$telecom$FormattedLog$OpType[OpType.DUMP.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    /* access modifiers changed from: private */
    public static String opTypeToString(OpType type) {
        int i = AnonymousClass1.$SwitchMap$mediatek$telecom$FormattedLog$OpType[type.ordinal()];
        if (i == 1) {
            return "OP";
        }
        if (i == 2) {
            return "Notify";
        }
        if (i != 3) {
            return null;
        }
        return "Dump";
    }

    public static class Builder {
        private String mAction;
        private String mCallId;
        private String mCallNumber;
        private String mCategory;
        private StringBuilder mExtraMessage = new StringBuilder();
        private OpType mOpType;
        private String mServiceName;
        private LinkedHashMap<String, String> mStatusInfo = new LinkedHashMap<>();

        public synchronized Builder setCategory(String category) {
            this.mCategory = category;
            return this;
        }

        public synchronized Builder setServiceName(String serviceName) {
            this.mServiceName = serviceName;
            return this;
        }

        public synchronized Builder setOpType(OpType type) {
            this.mOpType = type;
            return this;
        }

        public synchronized Builder setActionName(String action) {
            this.mAction = action;
            return this;
        }

        public synchronized Builder setCallNumber(String number) {
            if (number != null) {
                if (!number.equals("conferenceCall") && !PhoneNumberUtils.isUriNumber(number)) {
                    this.mCallNumber = PhoneNumberUtils.extractNetworkPortionAlt(number);
                }
            }
            this.mCallNumber = number;
            return this;
        }

        public synchronized Builder setCallId(String id) {
            this.mCallId = id;
            return this;
        }

        public synchronized Builder setStatusInfo(String name, String value) {
            if (!(name == null || value == null)) {
                if (!name.isEmpty() && !value.isEmpty()) {
                    this.mStatusInfo.put(name, value);
                }
            }
            return this;
        }

        public synchronized Builder resetStatusInfo(String name) {
            if (name != null) {
                if (!name.isEmpty()) {
                    this.mStatusInfo.remove(name);
                }
            }
            return this;
        }

        public synchronized Builder setExtraMessage(String msg) {
            if (msg != null) {
                this.mExtraMessage = new StringBuilder();
                this.mExtraMessage.append(msg);
            }
            return this;
        }

        public synchronized Builder appendExtraMessage(String msg) {
            if (msg != null) {
                this.mExtraMessage.append(msg);
            }
            return this;
        }

        public synchronized FormattedLog buildDebugMsg() {
            if (this.mCallNumber == null) {
                this.mCallNumber = "unknown";
            }
            return new FormattedLog("[Debug][%s][%s][%s][%s][%s][%s] %s", new Object[]{this.mCategory, this.mServiceName, FormattedLog.opTypeToString(this.mOpType), this.mAction, this.mCallNumber, this.mCallId, this.mExtraMessage}, null);
        }

        public synchronized FormattedLog buildDumpInfo() {
            StringBuilder statusInfo;
            statusInfo = new StringBuilder();
            Iterator<Map.Entry<String, String>> entryIterator = this.mStatusInfo.entrySet().iterator();
            while (entryIterator.hasNext()) {
                Map.Entry<String, String> entry = entryIterator.next();
                statusInfo.append("[");
                statusInfo.append(entry.getKey());
                statusInfo.append(":");
                statusInfo.append(entry.getValue());
                statusInfo.append("]");
                if (entryIterator.hasNext()) {
                    statusInfo.append(",");
                }
            }
            if (this.mCallNumber == null) {
                this.mCallNumber = "unknown";
            }
            return new FormattedLog("[Debug][%s][%s][Dump][%s][%s]-%s-%s", new Object[]{this.mCategory, this.mServiceName, this.mCallNumber, this.mCallId, statusInfo, this.mExtraMessage}, null);
        }
    }

    private FormattedLog(String format, Object... args) {
        String str;
        if (args != null) {
            try {
                if (args.length != 0) {
                    str = String.format(Locale.US, format, args);
                    this.mLogString = str;
                }
            } catch (IllegalFormatException e) {
                this.mLogString = format + " (An error occurred while formatting the message.)";
                return;
            }
        }
        str = format;
        this.mLogString = str;
    }

    public String toString() {
        return this.mLogString;
    }
}
