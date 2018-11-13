package com.qualcomm.qti.telephonyservice;

import com.google.protobuf.micro.ByteStringMicro;
import com.google.protobuf.micro.CodedInputStreamMicro;
import com.google.protobuf.micro.CodedOutputStreamMicro;
import com.google.protobuf.micro.InvalidProtocolBufferMicroException;
import com.google.protobuf.micro.MessageMicro;
import java.io.IOException;

public final class TelephonyServiceProtos {
    public static final int APPTYPE_CSIM = 4;
    public static final int APPTYPE_ISIM = 5;
    public static final int APPTYPE_RUIM = 3;
    public static final int APPTYPE_SIM = 1;
    public static final int APPTYPE_UNKNOWN = 0;
    public static final int APPTYPE_USIM = 2;
    public static final int KS_NAF_TYPE_ENCRYPTED = 1;
    public static final int KS_NAF_TYPE_PLAIN = 0;
    public static final int RIL_OEM_ERR_GENERIC_FAILURE = 1;
    public static final int RIL_OEM_ERR_INVALID_PARAMETER = 3;
    public static final int RIL_OEM_ERR_NOT_SUPPORTED = 2;
    public static final int RIL_OEM_ERR_SUCCESS = 0;
    public static final int RIL_OEM_GBA_INIT = 1;
    public static final int RIL_OEM_IMPI = 2;
    public static final int RIL_OEM_MSG_INDICATION = 3;
    public static final int RIL_OEM_MSG_REQUEST = 1;
    public static final int RIL_OEM_MSG_RESPONSE = 2;
    public static final int SLOT_ID_ONE = 0;
    public static final int SLOT_ID_THREE = 2;
    public static final int SLOT_ID_TWO = 1;

    public static final class RILOEMMessage extends MessageMicro {
        public static final int ERROR_FIELD_NUMBER = 4;
        public static final int ID_FIELD_NUMBER = 3;
        public static final int PAYLOAD_FIELD_NUMBER = 5;
        public static final int TOKEN_FIELD_NUMBER = 1;
        public static final int TYPE_FIELD_NUMBER = 2;
        private int cachedSize = -1;
        private int error_ = 0;
        private boolean hasError;
        private boolean hasId;
        private boolean hasPayload;
        private boolean hasToken;
        private boolean hasType;
        private int id_ = 1;
        private ByteStringMicro payload_ = ByteStringMicro.EMPTY;
        private int token_ = 0;
        private int type_ = 1;

        public int getToken() {
            return this.token_;
        }

        public boolean hasToken() {
            return this.hasToken;
        }

        public RILOEMMessage setToken(int value) {
            this.hasToken = true;
            this.token_ = value;
            return this;
        }

        public RILOEMMessage clearToken() {
            this.hasToken = false;
            this.token_ = 0;
            return this;
        }

        public boolean hasType() {
            return this.hasType;
        }

        public int getType() {
            return this.type_;
        }

        public RILOEMMessage setType(int value) {
            this.hasType = true;
            this.type_ = value;
            return this;
        }

        public RILOEMMessage clearType() {
            this.hasType = false;
            this.type_ = 1;
            return this;
        }

        public boolean hasId() {
            return this.hasId;
        }

        public int getId() {
            return this.id_;
        }

        public RILOEMMessage setId(int value) {
            this.hasId = true;
            this.id_ = value;
            return this;
        }

        public RILOEMMessage clearId() {
            this.hasId = false;
            this.id_ = 1;
            return this;
        }

        public boolean hasError() {
            return this.hasError;
        }

        public int getError() {
            return this.error_;
        }

        public RILOEMMessage setError(int value) {
            this.hasError = true;
            this.error_ = value;
            return this;
        }

        public RILOEMMessage clearError() {
            this.hasError = false;
            this.error_ = 0;
            return this;
        }

        public ByteStringMicro getPayload() {
            return this.payload_;
        }

        public boolean hasPayload() {
            return this.hasPayload;
        }

        public RILOEMMessage setPayload(ByteStringMicro value) {
            this.hasPayload = true;
            this.payload_ = value;
            return this;
        }

        public RILOEMMessage clearPayload() {
            this.hasPayload = false;
            this.payload_ = ByteStringMicro.EMPTY;
            return this;
        }

        public final RILOEMMessage clear() {
            clearToken();
            clearType();
            clearId();
            clearError();
            clearPayload();
            this.cachedSize = -1;
            return this;
        }

        public final boolean isInitialized() {
            if (this.hasToken && this.hasType && this.hasId) {
                return true;
            }
            return false;
        }

        public void writeTo(CodedOutputStreamMicro output) throws IOException {
            if (hasToken()) {
                output.writeFixed32(1, getToken());
            }
            if (hasType()) {
                output.writeInt32(2, getType());
            }
            if (hasId()) {
                output.writeInt32(3, getId());
            }
            if (hasError()) {
                output.writeInt32(4, getError());
            }
            if (hasPayload()) {
                output.writeBytes(5, getPayload());
            }
        }

        public int getCachedSize() {
            if (this.cachedSize < 0) {
                getSerializedSize();
            }
            return this.cachedSize;
        }

        public int getSerializedSize() {
            int size = 0;
            if (hasToken()) {
                size = CodedOutputStreamMicro.computeFixed32Size(1, getToken()) + 0;
            }
            if (hasType()) {
                size += CodedOutputStreamMicro.computeInt32Size(2, getType());
            }
            if (hasId()) {
                size += CodedOutputStreamMicro.computeInt32Size(3, getId());
            }
            if (hasError()) {
                size += CodedOutputStreamMicro.computeInt32Size(4, getError());
            }
            if (hasPayload()) {
                size += CodedOutputStreamMicro.computeBytesSize(5, getPayload());
            }
            this.cachedSize = size;
            return size;
        }

        public RILOEMMessage mergeFrom(CodedInputStreamMicro input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case 13:
                        setToken(input.readFixed32());
                        break;
                    case 16:
                        setType(input.readInt32());
                        break;
                    case 24:
                        setId(input.readInt32());
                        break;
                    case 32:
                        setError(input.readInt32());
                        break;
                    case 42:
                        setPayload(input.readBytes());
                        break;
                    default:
                        if (parseUnknownField(input, tag)) {
                            break;
                        }
                        return this;
                }
            }
        }

        public static RILOEMMessage parseFrom(byte[] data) throws InvalidProtocolBufferMicroException {
            return (RILOEMMessage) new RILOEMMessage().mergeFrom(data);
        }

        public static RILOEMMessage parseFrom(CodedInputStreamMicro input) throws IOException {
            return new RILOEMMessage().mergeFrom(input);
        }
    }

    public static final class RilOemGbaInitRequestPayload extends MessageMicro {
        public static final int APPLICATIONTYPE_FIELD_NUMBER = 4;
        public static final int FORCEBOOTSTRAPPING_FIELD_NUMBER = 5;
        public static final int NAFFULLYQUALIFIEDDOMAINNAME_FIELD_NUMBER = 2;
        public static final int SECURITYPROTOCOL_FIELD_NUMBER = 1;
        public static final int SLOTID_FIELD_NUMBER = 3;
        private int applicationType_ = 0;
        private int cachedSize = -1;
        private boolean forceBootstrapping_ = false;
        private boolean hasApplicationType;
        private boolean hasForceBootstrapping;
        private boolean hasNafFullyQualifiedDomainName;
        private boolean hasSecurityProtocol;
        private boolean hasSlotId;
        private String nafFullyQualifiedDomainName_ = "";
        private ByteStringMicro securityProtocol_ = ByteStringMicro.EMPTY;
        private int slotId_ = 0;

        public ByteStringMicro getSecurityProtocol() {
            return this.securityProtocol_;
        }

        public boolean hasSecurityProtocol() {
            return this.hasSecurityProtocol;
        }

        public RilOemGbaInitRequestPayload setSecurityProtocol(ByteStringMicro value) {
            this.hasSecurityProtocol = true;
            this.securityProtocol_ = value;
            return this;
        }

        public RilOemGbaInitRequestPayload clearSecurityProtocol() {
            this.hasSecurityProtocol = false;
            this.securityProtocol_ = ByteStringMicro.EMPTY;
            return this;
        }

        public String getNafFullyQualifiedDomainName() {
            return this.nafFullyQualifiedDomainName_;
        }

        public boolean hasNafFullyQualifiedDomainName() {
            return this.hasNafFullyQualifiedDomainName;
        }

        public RilOemGbaInitRequestPayload setNafFullyQualifiedDomainName(String value) {
            this.hasNafFullyQualifiedDomainName = true;
            this.nafFullyQualifiedDomainName_ = value;
            return this;
        }

        public RilOemGbaInitRequestPayload clearNafFullyQualifiedDomainName() {
            this.hasNafFullyQualifiedDomainName = false;
            this.nafFullyQualifiedDomainName_ = "";
            return this;
        }

        public boolean hasSlotId() {
            return this.hasSlotId;
        }

        public int getSlotId() {
            return this.slotId_;
        }

        public RilOemGbaInitRequestPayload setSlotId(int value) {
            this.hasSlotId = true;
            this.slotId_ = value;
            return this;
        }

        public RilOemGbaInitRequestPayload clearSlotId() {
            this.hasSlotId = false;
            this.slotId_ = 0;
            return this;
        }

        public boolean hasApplicationType() {
            return this.hasApplicationType;
        }

        public int getApplicationType() {
            return this.applicationType_;
        }

        public RilOemGbaInitRequestPayload setApplicationType(int value) {
            this.hasApplicationType = true;
            this.applicationType_ = value;
            return this;
        }

        public RilOemGbaInitRequestPayload clearApplicationType() {
            this.hasApplicationType = false;
            this.applicationType_ = 0;
            return this;
        }

        public boolean getForceBootstrapping() {
            return this.forceBootstrapping_;
        }

        public boolean hasForceBootstrapping() {
            return this.hasForceBootstrapping;
        }

        public RilOemGbaInitRequestPayload setForceBootstrapping(boolean value) {
            this.hasForceBootstrapping = true;
            this.forceBootstrapping_ = value;
            return this;
        }

        public RilOemGbaInitRequestPayload clearForceBootstrapping() {
            this.hasForceBootstrapping = false;
            this.forceBootstrapping_ = false;
            return this;
        }

        public final RilOemGbaInitRequestPayload clear() {
            clearSecurityProtocol();
            clearNafFullyQualifiedDomainName();
            clearSlotId();
            clearApplicationType();
            clearForceBootstrapping();
            this.cachedSize = -1;
            return this;
        }

        public final boolean isInitialized() {
            if (this.hasSecurityProtocol && this.hasNafFullyQualifiedDomainName && this.hasSlotId && this.hasApplicationType) {
                return true;
            }
            return false;
        }

        public void writeTo(CodedOutputStreamMicro output) throws IOException {
            if (hasSecurityProtocol()) {
                output.writeBytes(1, getSecurityProtocol());
            }
            if (hasNafFullyQualifiedDomainName()) {
                output.writeString(2, getNafFullyQualifiedDomainName());
            }
            if (hasSlotId()) {
                output.writeInt32(3, getSlotId());
            }
            if (hasApplicationType()) {
                output.writeInt32(4, getApplicationType());
            }
            if (hasForceBootstrapping()) {
                output.writeBool(5, getForceBootstrapping());
            }
        }

        public int getCachedSize() {
            if (this.cachedSize < 0) {
                getSerializedSize();
            }
            return this.cachedSize;
        }

        public int getSerializedSize() {
            int size = 0;
            if (hasSecurityProtocol()) {
                size = CodedOutputStreamMicro.computeBytesSize(1, getSecurityProtocol()) + 0;
            }
            if (hasNafFullyQualifiedDomainName()) {
                size += CodedOutputStreamMicro.computeStringSize(2, getNafFullyQualifiedDomainName());
            }
            if (hasSlotId()) {
                size += CodedOutputStreamMicro.computeInt32Size(3, getSlotId());
            }
            if (hasApplicationType()) {
                size += CodedOutputStreamMicro.computeInt32Size(4, getApplicationType());
            }
            if (hasForceBootstrapping()) {
                size += CodedOutputStreamMicro.computeBoolSize(5, getForceBootstrapping());
            }
            this.cachedSize = size;
            return size;
        }

        public RilOemGbaInitRequestPayload mergeFrom(CodedInputStreamMicro input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case 10:
                        setSecurityProtocol(input.readBytes());
                        break;
                    case 18:
                        setNafFullyQualifiedDomainName(input.readString());
                        break;
                    case 24:
                        setSlotId(input.readInt32());
                        break;
                    case 32:
                        setApplicationType(input.readInt32());
                        break;
                    case 40:
                        setForceBootstrapping(input.readBool());
                        break;
                    default:
                        if (parseUnknownField(input, tag)) {
                            break;
                        }
                        return this;
                }
            }
        }

        public static RilOemGbaInitRequestPayload parseFrom(byte[] data) throws InvalidProtocolBufferMicroException {
            return (RilOemGbaInitRequestPayload) new RilOemGbaInitRequestPayload().mergeFrom(data);
        }

        public static RilOemGbaInitRequestPayload parseFrom(CodedInputStreamMicro input) throws IOException {
            return new RilOemGbaInitRequestPayload().mergeFrom(input);
        }
    }

    public static final class RilOemGbaInitResponsePayload extends MessageMicro {
        public static final int BOOTSTRAPTRANSACTIONID_FIELD_NUMBER = 4;
        public static final int IMPI_FIELD_NUMBER = 3;
        public static final int KSLIFETIME_FIELD_NUMBER = 5;
        public static final int KSNAFRESPONSE_FIELD_NUMBER = 2;
        public static final int KSNAFTYPE_FIELD_NUMBER = 1;
        private String bootstrapTransactionId_ = "";
        private int cachedSize = -1;
        private boolean hasBootstrapTransactionId;
        private boolean hasImpi;
        private boolean hasKsLifetime;
        private boolean hasKsNAFResponse;
        private boolean hasKsNAFType;
        private ByteStringMicro impi_ = ByteStringMicro.EMPTY;
        private String ksLifetime_ = "";
        private ByteStringMicro ksNAFResponse_ = ByteStringMicro.EMPTY;
        private int ksNAFType_ = 0;

        public boolean hasKsNAFType() {
            return this.hasKsNAFType;
        }

        public int getKsNAFType() {
            return this.ksNAFType_;
        }

        public RilOemGbaInitResponsePayload setKsNAFType(int value) {
            this.hasKsNAFType = true;
            this.ksNAFType_ = value;
            return this;
        }

        public RilOemGbaInitResponsePayload clearKsNAFType() {
            this.hasKsNAFType = false;
            this.ksNAFType_ = 0;
            return this;
        }

        public ByteStringMicro getKsNAFResponse() {
            return this.ksNAFResponse_;
        }

        public boolean hasKsNAFResponse() {
            return this.hasKsNAFResponse;
        }

        public RilOemGbaInitResponsePayload setKsNAFResponse(ByteStringMicro value) {
            this.hasKsNAFResponse = true;
            this.ksNAFResponse_ = value;
            return this;
        }

        public RilOemGbaInitResponsePayload clearKsNAFResponse() {
            this.hasKsNAFResponse = false;
            this.ksNAFResponse_ = ByteStringMicro.EMPTY;
            return this;
        }

        public ByteStringMicro getImpi() {
            return this.impi_;
        }

        public boolean hasImpi() {
            return this.hasImpi;
        }

        public RilOemGbaInitResponsePayload setImpi(ByteStringMicro value) {
            this.hasImpi = true;
            this.impi_ = value;
            return this;
        }

        public RilOemGbaInitResponsePayload clearImpi() {
            this.hasImpi = false;
            this.impi_ = ByteStringMicro.EMPTY;
            return this;
        }

        public String getBootstrapTransactionId() {
            return this.bootstrapTransactionId_;
        }

        public boolean hasBootstrapTransactionId() {
            return this.hasBootstrapTransactionId;
        }

        public RilOemGbaInitResponsePayload setBootstrapTransactionId(String value) {
            this.hasBootstrapTransactionId = true;
            this.bootstrapTransactionId_ = value;
            return this;
        }

        public RilOemGbaInitResponsePayload clearBootstrapTransactionId() {
            this.hasBootstrapTransactionId = false;
            this.bootstrapTransactionId_ = "";
            return this;
        }

        public String getKsLifetime() {
            return this.ksLifetime_;
        }

        public boolean hasKsLifetime() {
            return this.hasKsLifetime;
        }

        public RilOemGbaInitResponsePayload setKsLifetime(String value) {
            this.hasKsLifetime = true;
            this.ksLifetime_ = value;
            return this;
        }

        public RilOemGbaInitResponsePayload clearKsLifetime() {
            this.hasKsLifetime = false;
            this.ksLifetime_ = "";
            return this;
        }

        public final RilOemGbaInitResponsePayload clear() {
            clearKsNAFType();
            clearKsNAFResponse();
            clearImpi();
            clearBootstrapTransactionId();
            clearKsLifetime();
            this.cachedSize = -1;
            return this;
        }

        public final boolean isInitialized() {
            if (this.hasKsNAFType && this.hasKsNAFResponse && this.hasImpi && this.hasBootstrapTransactionId && this.hasKsLifetime) {
                return true;
            }
            return false;
        }

        public void writeTo(CodedOutputStreamMicro output) throws IOException {
            if (hasKsNAFType()) {
                output.writeInt32(1, getKsNAFType());
            }
            if (hasKsNAFResponse()) {
                output.writeBytes(2, getKsNAFResponse());
            }
            if (hasImpi()) {
                output.writeBytes(3, getImpi());
            }
            if (hasBootstrapTransactionId()) {
                output.writeString(4, getBootstrapTransactionId());
            }
            if (hasKsLifetime()) {
                output.writeString(5, getKsLifetime());
            }
        }

        public int getCachedSize() {
            if (this.cachedSize < 0) {
                getSerializedSize();
            }
            return this.cachedSize;
        }

        public int getSerializedSize() {
            int size = 0;
            if (hasKsNAFType()) {
                size = CodedOutputStreamMicro.computeInt32Size(1, getKsNAFType()) + 0;
            }
            if (hasKsNAFResponse()) {
                size += CodedOutputStreamMicro.computeBytesSize(2, getKsNAFResponse());
            }
            if (hasImpi()) {
                size += CodedOutputStreamMicro.computeBytesSize(3, getImpi());
            }
            if (hasBootstrapTransactionId()) {
                size += CodedOutputStreamMicro.computeStringSize(4, getBootstrapTransactionId());
            }
            if (hasKsLifetime()) {
                size += CodedOutputStreamMicro.computeStringSize(5, getKsLifetime());
            }
            this.cachedSize = size;
            return size;
        }

        public RilOemGbaInitResponsePayload mergeFrom(CodedInputStreamMicro input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case CodedOutputStreamMicro.LITTLE_ENDIAN_64_SIZE /*8*/:
                        setKsNAFType(input.readInt32());
                        break;
                    case 18:
                        setKsNAFResponse(input.readBytes());
                        break;
                    case 26:
                        setImpi(input.readBytes());
                        break;
                    case 34:
                        setBootstrapTransactionId(input.readString());
                        break;
                    case 42:
                        setKsLifetime(input.readString());
                        break;
                    default:
                        if (parseUnknownField(input, tag)) {
                            break;
                        }
                        return this;
                }
            }
        }

        public static RilOemGbaInitResponsePayload parseFrom(byte[] data) throws InvalidProtocolBufferMicroException {
            return (RilOemGbaInitResponsePayload) new RilOemGbaInitResponsePayload().mergeFrom(data);
        }

        public static RilOemGbaInitResponsePayload parseFrom(CodedInputStreamMicro input) throws IOException {
            return new RilOemGbaInitResponsePayload().mergeFrom(input);
        }
    }

    public static final class RilOemImpiRequestPayload extends MessageMicro {
        public static final int APPLICATIONTYPE_FIELD_NUMBER = 2;
        public static final int SECURE_FIELD_NUMBER = 3;
        public static final int SLOTID_FIELD_NUMBER = 1;
        private int applicationType_ = 0;
        private int cachedSize = -1;
        private boolean hasApplicationType;
        private boolean hasSecure;
        private boolean hasSlotId;
        private boolean secure_ = false;
        private int slotId_ = 0;

        public boolean hasSlotId() {
            return this.hasSlotId;
        }

        public int getSlotId() {
            return this.slotId_;
        }

        public RilOemImpiRequestPayload setSlotId(int value) {
            this.hasSlotId = true;
            this.slotId_ = value;
            return this;
        }

        public RilOemImpiRequestPayload clearSlotId() {
            this.hasSlotId = false;
            this.slotId_ = 0;
            return this;
        }

        public boolean hasApplicationType() {
            return this.hasApplicationType;
        }

        public int getApplicationType() {
            return this.applicationType_;
        }

        public RilOemImpiRequestPayload setApplicationType(int value) {
            this.hasApplicationType = true;
            this.applicationType_ = value;
            return this;
        }

        public RilOemImpiRequestPayload clearApplicationType() {
            this.hasApplicationType = false;
            this.applicationType_ = 0;
            return this;
        }

        public boolean getSecure() {
            return this.secure_;
        }

        public boolean hasSecure() {
            return this.hasSecure;
        }

        public RilOemImpiRequestPayload setSecure(boolean value) {
            this.hasSecure = true;
            this.secure_ = value;
            return this;
        }

        public RilOemImpiRequestPayload clearSecure() {
            this.hasSecure = false;
            this.secure_ = false;
            return this;
        }

        public final RilOemImpiRequestPayload clear() {
            clearSlotId();
            clearApplicationType();
            clearSecure();
            this.cachedSize = -1;
            return this;
        }

        public final boolean isInitialized() {
            if (this.hasSlotId && this.hasApplicationType && this.hasSecure) {
                return true;
            }
            return false;
        }

        public void writeTo(CodedOutputStreamMicro output) throws IOException {
            if (hasSlotId()) {
                output.writeInt32(1, getSlotId());
            }
            if (hasApplicationType()) {
                output.writeInt32(2, getApplicationType());
            }
            if (hasSecure()) {
                output.writeBool(3, getSecure());
            }
        }

        public int getCachedSize() {
            if (this.cachedSize < 0) {
                getSerializedSize();
            }
            return this.cachedSize;
        }

        public int getSerializedSize() {
            int size = 0;
            if (hasSlotId()) {
                size = CodedOutputStreamMicro.computeInt32Size(1, getSlotId()) + 0;
            }
            if (hasApplicationType()) {
                size += CodedOutputStreamMicro.computeInt32Size(2, getApplicationType());
            }
            if (hasSecure()) {
                size += CodedOutputStreamMicro.computeBoolSize(3, getSecure());
            }
            this.cachedSize = size;
            return size;
        }

        public RilOemImpiRequestPayload mergeFrom(CodedInputStreamMicro input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case CodedOutputStreamMicro.LITTLE_ENDIAN_64_SIZE /*8*/:
                        setSlotId(input.readInt32());
                        break;
                    case 16:
                        setApplicationType(input.readInt32());
                        break;
                    case 24:
                        setSecure(input.readBool());
                        break;
                    default:
                        if (parseUnknownField(input, tag)) {
                            break;
                        }
                        return this;
                }
            }
        }

        public static RilOemImpiRequestPayload parseFrom(byte[] data) throws InvalidProtocolBufferMicroException {
            return (RilOemImpiRequestPayload) new RilOemImpiRequestPayload().mergeFrom(data);
        }

        public static RilOemImpiRequestPayload parseFrom(CodedInputStreamMicro input) throws IOException {
            return new RilOemImpiRequestPayload().mergeFrom(input);
        }
    }

    public static final class RilOemImpiResponsePayload extends MessageMicro {
        public static final int IMPI_FIELD_NUMBER = 1;
        private int cachedSize = -1;
        private boolean hasImpi;
        private ByteStringMicro impi_ = ByteStringMicro.EMPTY;

        public ByteStringMicro getImpi() {
            return this.impi_;
        }

        public boolean hasImpi() {
            return this.hasImpi;
        }

        public RilOemImpiResponsePayload setImpi(ByteStringMicro value) {
            this.hasImpi = true;
            this.impi_ = value;
            return this;
        }

        public RilOemImpiResponsePayload clearImpi() {
            this.hasImpi = false;
            this.impi_ = ByteStringMicro.EMPTY;
            return this;
        }

        public final RilOemImpiResponsePayload clear() {
            clearImpi();
            this.cachedSize = -1;
            return this;
        }

        public final boolean isInitialized() {
            if (this.hasImpi) {
                return true;
            }
            return false;
        }

        public void writeTo(CodedOutputStreamMicro output) throws IOException {
            if (hasImpi()) {
                output.writeBytes(1, getImpi());
            }
        }

        public int getCachedSize() {
            if (this.cachedSize < 0) {
                getSerializedSize();
            }
            return this.cachedSize;
        }

        public int getSerializedSize() {
            int size = 0;
            if (hasImpi()) {
                size = CodedOutputStreamMicro.computeBytesSize(1, getImpi()) + 0;
            }
            this.cachedSize = size;
            return size;
        }

        public RilOemImpiResponsePayload mergeFrom(CodedInputStreamMicro input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case 10:
                        setImpi(input.readBytes());
                        break;
                    default:
                        if (parseUnknownField(input, tag)) {
                            break;
                        }
                        return this;
                }
            }
        }

        public static RilOemImpiResponsePayload parseFrom(byte[] data) throws InvalidProtocolBufferMicroException {
            return (RilOemImpiResponsePayload) new RilOemImpiResponsePayload().mergeFrom(data);
        }

        public static RilOemImpiResponsePayload parseFrom(CodedInputStreamMicro input) throws IOException {
            return new RilOemImpiResponsePayload().mergeFrom(input);
        }
    }

    private TelephonyServiceProtos() {
    }
}
