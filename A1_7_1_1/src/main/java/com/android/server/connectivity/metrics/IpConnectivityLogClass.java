package com.android.server.connectivity.metrics;

import com.android.server.oppo.IElsaManager;
import com.google.protobuf.nano.CodedInputByteBufferNano;
import com.google.protobuf.nano.CodedOutputByteBufferNano;
import com.google.protobuf.nano.InternalNano;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.google.protobuf.nano.MessageNano;
import com.google.protobuf.nano.WireFormatNano;
import java.io.IOException;

public interface IpConnectivityLogClass {

    public static final class ApfProgramEvent extends MessageNano {
        private static volatile ApfProgramEvent[] _emptyArray;
        public int currentRas;
        public boolean dropMulticast;
        public int filteredRas;
        public boolean hasIpv4Addr;
        public long lifetime;
        public int programLength;

        public static ApfProgramEvent[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new ApfProgramEvent[0];
                    }
                }
            }
            return _emptyArray;
        }

        public ApfProgramEvent() {
            clear();
        }

        public ApfProgramEvent clear() {
            this.lifetime = 0;
            this.filteredRas = 0;
            this.currentRas = 0;
            this.programLength = 0;
            this.dropMulticast = false;
            this.hasIpv4Addr = false;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.lifetime != 0) {
                output.writeInt64(1, this.lifetime);
            }
            if (this.filteredRas != 0) {
                output.writeInt32(2, this.filteredRas);
            }
            if (this.currentRas != 0) {
                output.writeInt32(3, this.currentRas);
            }
            if (this.programLength != 0) {
                output.writeInt32(4, this.programLength);
            }
            if (this.dropMulticast) {
                output.writeBool(5, this.dropMulticast);
            }
            if (this.hasIpv4Addr) {
                output.writeBool(6, this.hasIpv4Addr);
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (this.lifetime != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(1, this.lifetime);
            }
            if (this.filteredRas != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, this.filteredRas);
            }
            if (this.currentRas != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, this.currentRas);
            }
            if (this.programLength != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(4, this.programLength);
            }
            if (this.dropMulticast) {
                size += CodedOutputByteBufferNano.computeBoolSize(5, this.dropMulticast);
            }
            if (this.hasIpv4Addr) {
                return size + CodedOutputByteBufferNano.computeBoolSize(6, this.hasIpv4Addr);
            }
            return size;
        }

        public ApfProgramEvent mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case 8:
                        this.lifetime = input.readInt64();
                        break;
                    case 16:
                        this.filteredRas = input.readInt32();
                        break;
                    case 24:
                        this.currentRas = input.readInt32();
                        break;
                    case 32:
                        this.programLength = input.readInt32();
                        break;
                    case 40:
                        this.dropMulticast = input.readBool();
                        break;
                    case 48:
                        this.hasIpv4Addr = input.readBool();
                        break;
                    default:
                        if (WireFormatNano.parseUnknownField(input, tag)) {
                            break;
                        }
                        return this;
                }
            }
        }

        public static ApfProgramEvent parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (ApfProgramEvent) MessageNano.mergeFrom(new ApfProgramEvent(), data);
        }

        public static ApfProgramEvent parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new ApfProgramEvent().mergeFrom(input);
        }
    }

    public static final class ApfStatistics extends MessageNano {
        private static volatile ApfStatistics[] _emptyArray;
        public int droppedRas;
        public long durationMs;
        public int matchingRas;
        public int maxProgramSize;
        public int parseErrors;
        public int programUpdates;
        public int receivedRas;
        public int zeroLifetimeRas;

        public static ApfStatistics[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new ApfStatistics[0];
                    }
                }
            }
            return _emptyArray;
        }

        public ApfStatistics() {
            clear();
        }

        public ApfStatistics clear() {
            this.durationMs = 0;
            this.receivedRas = 0;
            this.matchingRas = 0;
            this.droppedRas = 0;
            this.zeroLifetimeRas = 0;
            this.parseErrors = 0;
            this.programUpdates = 0;
            this.maxProgramSize = 0;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.durationMs != 0) {
                output.writeInt64(1, this.durationMs);
            }
            if (this.receivedRas != 0) {
                output.writeInt32(2, this.receivedRas);
            }
            if (this.matchingRas != 0) {
                output.writeInt32(3, this.matchingRas);
            }
            if (this.droppedRas != 0) {
                output.writeInt32(5, this.droppedRas);
            }
            if (this.zeroLifetimeRas != 0) {
                output.writeInt32(6, this.zeroLifetimeRas);
            }
            if (this.parseErrors != 0) {
                output.writeInt32(7, this.parseErrors);
            }
            if (this.programUpdates != 0) {
                output.writeInt32(8, this.programUpdates);
            }
            if (this.maxProgramSize != 0) {
                output.writeInt32(9, this.maxProgramSize);
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (this.durationMs != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(1, this.durationMs);
            }
            if (this.receivedRas != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, this.receivedRas);
            }
            if (this.matchingRas != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, this.matchingRas);
            }
            if (this.droppedRas != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(5, this.droppedRas);
            }
            if (this.zeroLifetimeRas != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(6, this.zeroLifetimeRas);
            }
            if (this.parseErrors != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(7, this.parseErrors);
            }
            if (this.programUpdates != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(8, this.programUpdates);
            }
            if (this.maxProgramSize != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(9, this.maxProgramSize);
            }
            return size;
        }

        public ApfStatistics mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case 8:
                        this.durationMs = input.readInt64();
                        break;
                    case 16:
                        this.receivedRas = input.readInt32();
                        break;
                    case 24:
                        this.matchingRas = input.readInt32();
                        break;
                    case 40:
                        this.droppedRas = input.readInt32();
                        break;
                    case 48:
                        this.zeroLifetimeRas = input.readInt32();
                        break;
                    case 56:
                        this.parseErrors = input.readInt32();
                        break;
                    case 64:
                        this.programUpdates = input.readInt32();
                        break;
                    case HdmiCecKeycode.CEC_KEYCODE_REWIND /*72*/:
                        this.maxProgramSize = input.readInt32();
                        break;
                    default:
                        if (WireFormatNano.parseUnknownField(input, tag)) {
                            break;
                        }
                        return this;
                }
            }
        }

        public static ApfStatistics parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (ApfStatistics) MessageNano.mergeFrom(new ApfStatistics(), data);
        }

        public static ApfStatistics parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new ApfStatistics().mergeFrom(input);
        }
    }

    public static final class DHCPEvent extends MessageNano {
        private static volatile DHCPEvent[] _emptyArray;
        public int durationMs;
        public int errorCode;
        public String ifName;
        public String stateTransition;

        public static DHCPEvent[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new DHCPEvent[0];
                    }
                }
            }
            return _emptyArray;
        }

        public DHCPEvent() {
            clear();
        }

        public DHCPEvent clear() {
            this.ifName = IElsaManager.EMPTY_PACKAGE;
            this.stateTransition = IElsaManager.EMPTY_PACKAGE;
            this.errorCode = 0;
            this.durationMs = 0;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (!this.ifName.equals(IElsaManager.EMPTY_PACKAGE)) {
                output.writeString(1, this.ifName);
            }
            if (!this.stateTransition.equals(IElsaManager.EMPTY_PACKAGE)) {
                output.writeString(2, this.stateTransition);
            }
            if (this.errorCode != 0) {
                output.writeInt32(3, this.errorCode);
            }
            if (this.durationMs != 0) {
                output.writeInt32(4, this.durationMs);
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (!this.ifName.equals(IElsaManager.EMPTY_PACKAGE)) {
                size += CodedOutputByteBufferNano.computeStringSize(1, this.ifName);
            }
            if (!this.stateTransition.equals(IElsaManager.EMPTY_PACKAGE)) {
                size += CodedOutputByteBufferNano.computeStringSize(2, this.stateTransition);
            }
            if (this.errorCode != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, this.errorCode);
            }
            if (this.durationMs != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(4, this.durationMs);
            }
            return size;
        }

        public DHCPEvent mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case 10:
                        this.ifName = input.readString();
                        break;
                    case 18:
                        this.stateTransition = input.readString();
                        break;
                    case 24:
                        this.errorCode = input.readInt32();
                        break;
                    case 32:
                        this.durationMs = input.readInt32();
                        break;
                    default:
                        if (WireFormatNano.parseUnknownField(input, tag)) {
                            break;
                        }
                        return this;
                }
            }
        }

        public static DHCPEvent parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (DHCPEvent) MessageNano.mergeFrom(new DHCPEvent(), data);
        }

        public static DHCPEvent parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new DHCPEvent().mergeFrom(input);
        }
    }

    public static final class DNSLookupBatch extends MessageNano {
        private static volatile DNSLookupBatch[] _emptyArray;
        public int[] eventTypes;
        public int[] latenciesMs;
        public NetworkId networkId;
        public int[] returnCodes;

        public static DNSLookupBatch[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new DNSLookupBatch[0];
                    }
                }
            }
            return _emptyArray;
        }

        public DNSLookupBatch() {
            clear();
        }

        public DNSLookupBatch clear() {
            this.networkId = null;
            this.eventTypes = WireFormatNano.EMPTY_INT_ARRAY;
            this.returnCodes = WireFormatNano.EMPTY_INT_ARRAY;
            this.latenciesMs = WireFormatNano.EMPTY_INT_ARRAY;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.networkId != null) {
                output.writeMessage(1, this.networkId);
            }
            if (this.eventTypes != null && this.eventTypes.length > 0) {
                for (int writeInt32 : this.eventTypes) {
                    output.writeInt32(2, writeInt32);
                }
            }
            if (this.returnCodes != null && this.returnCodes.length > 0) {
                for (int writeInt322 : this.returnCodes) {
                    output.writeInt32(3, writeInt322);
                }
            }
            if (this.latenciesMs != null && this.latenciesMs.length > 0) {
                for (int writeInt3222 : this.latenciesMs) {
                    output.writeInt32(4, writeInt3222);
                }
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int dataSize;
            int size = super.computeSerializedSize();
            if (this.networkId != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(1, this.networkId);
            }
            if (this.eventTypes != null && this.eventTypes.length > 0) {
                dataSize = 0;
                for (int element : this.eventTypes) {
                    dataSize += CodedOutputByteBufferNano.computeInt32SizeNoTag(element);
                }
                size = (size + dataSize) + (this.eventTypes.length * 1);
            }
            if (this.returnCodes != null && this.returnCodes.length > 0) {
                dataSize = 0;
                for (int element2 : this.returnCodes) {
                    dataSize += CodedOutputByteBufferNano.computeInt32SizeNoTag(element2);
                }
                size = (size + dataSize) + (this.returnCodes.length * 1);
            }
            if (this.latenciesMs == null || this.latenciesMs.length <= 0) {
                return size;
            }
            dataSize = 0;
            for (int element22 : this.latenciesMs) {
                dataSize += CodedOutputByteBufferNano.computeInt32SizeNoTag(element22);
            }
            return (size + dataSize) + (this.latenciesMs.length * 1);
        }

        public DNSLookupBatch mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                int arrayLength;
                int i;
                int[] newArray;
                int limit;
                int startPos;
                switch (tag) {
                    case 0:
                        return this;
                    case 10:
                        if (this.networkId == null) {
                            this.networkId = new NetworkId();
                        }
                        input.readMessage(this.networkId);
                        break;
                    case 16:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 16);
                        i = this.eventTypes == null ? 0 : this.eventTypes.length;
                        newArray = new int[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.eventTypes, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = input.readInt32();
                            input.readTag();
                            i++;
                        }
                        newArray[i] = input.readInt32();
                        this.eventTypes = newArray;
                        break;
                    case 18:
                        limit = input.pushLimit(input.readRawVarint32());
                        arrayLength = 0;
                        startPos = input.getPosition();
                        while (input.getBytesUntilLimit() > 0) {
                            input.readInt32();
                            arrayLength++;
                        }
                        input.rewindToPosition(startPos);
                        i = this.eventTypes == null ? 0 : this.eventTypes.length;
                        newArray = new int[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.eventTypes, 0, newArray, 0, i);
                        }
                        while (i < newArray.length) {
                            newArray[i] = input.readInt32();
                            i++;
                        }
                        this.eventTypes = newArray;
                        input.popLimit(limit);
                        break;
                    case 24:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 24);
                        i = this.returnCodes == null ? 0 : this.returnCodes.length;
                        newArray = new int[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.returnCodes, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = input.readInt32();
                            input.readTag();
                            i++;
                        }
                        newArray[i] = input.readInt32();
                        this.returnCodes = newArray;
                        break;
                    case H.DO_ANIMATION_CALLBACK /*26*/:
                        limit = input.pushLimit(input.readRawVarint32());
                        arrayLength = 0;
                        startPos = input.getPosition();
                        while (input.getBytesUntilLimit() > 0) {
                            input.readInt32();
                            arrayLength++;
                        }
                        input.rewindToPosition(startPos);
                        i = this.returnCodes == null ? 0 : this.returnCodes.length;
                        newArray = new int[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.returnCodes, 0, newArray, 0, i);
                        }
                        while (i < newArray.length) {
                            newArray[i] = input.readInt32();
                            i++;
                        }
                        this.returnCodes = newArray;
                        input.popLimit(limit);
                        break;
                    case 32:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 32);
                        i = this.latenciesMs == null ? 0 : this.latenciesMs.length;
                        newArray = new int[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.latenciesMs, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = input.readInt32();
                            input.readTag();
                            i++;
                        }
                        newArray[i] = input.readInt32();
                        this.latenciesMs = newArray;
                        break;
                    case 34:
                        limit = input.pushLimit(input.readRawVarint32());
                        arrayLength = 0;
                        startPos = input.getPosition();
                        while (input.getBytesUntilLimit() > 0) {
                            input.readInt32();
                            arrayLength++;
                        }
                        input.rewindToPosition(startPos);
                        i = this.latenciesMs == null ? 0 : this.latenciesMs.length;
                        newArray = new int[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.latenciesMs, 0, newArray, 0, i);
                        }
                        while (i < newArray.length) {
                            newArray[i] = input.readInt32();
                            i++;
                        }
                        this.latenciesMs = newArray;
                        input.popLimit(limit);
                        break;
                    default:
                        if (WireFormatNano.parseUnknownField(input, tag)) {
                            break;
                        }
                        return this;
                }
            }
        }

        public static DNSLookupBatch parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (DNSLookupBatch) MessageNano.mergeFrom(new DNSLookupBatch(), data);
        }

        public static DNSLookupBatch parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new DNSLookupBatch().mergeFrom(input);
        }
    }

    public static final class DefaultNetworkEvent extends MessageNano {
        public static final int DUAL = 3;
        public static final int IPV4 = 1;
        public static final int IPV6 = 2;
        public static final int NONE = 0;
        private static volatile DefaultNetworkEvent[] _emptyArray;
        public NetworkId networkId;
        public NetworkId previousNetworkId;
        public int previousNetworkIpSupport;
        public int[] transportTypes;

        public static DefaultNetworkEvent[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new DefaultNetworkEvent[0];
                    }
                }
            }
            return _emptyArray;
        }

        public DefaultNetworkEvent() {
            clear();
        }

        public DefaultNetworkEvent clear() {
            this.networkId = null;
            this.previousNetworkId = null;
            this.previousNetworkIpSupport = 0;
            this.transportTypes = WireFormatNano.EMPTY_INT_ARRAY;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.networkId != null) {
                output.writeMessage(1, this.networkId);
            }
            if (this.previousNetworkId != null) {
                output.writeMessage(2, this.previousNetworkId);
            }
            if (this.previousNetworkIpSupport != 0) {
                output.writeInt32(3, this.previousNetworkIpSupport);
            }
            if (this.transportTypes != null && this.transportTypes.length > 0) {
                for (int writeInt32 : this.transportTypes) {
                    output.writeInt32(4, writeInt32);
                }
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (this.networkId != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(1, this.networkId);
            }
            if (this.previousNetworkId != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(2, this.previousNetworkId);
            }
            if (this.previousNetworkIpSupport != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, this.previousNetworkIpSupport);
            }
            if (this.transportTypes == null || this.transportTypes.length <= 0) {
                return size;
            }
            int dataSize = 0;
            for (int element : this.transportTypes) {
                dataSize += CodedOutputByteBufferNano.computeInt32SizeNoTag(element);
            }
            return (size + dataSize) + (this.transportTypes.length * 1);
        }

        public DefaultNetworkEvent mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                int arrayLength;
                int i;
                int[] newArray;
                switch (tag) {
                    case 0:
                        return this;
                    case 10:
                        if (this.networkId == null) {
                            this.networkId = new NetworkId();
                        }
                        input.readMessage(this.networkId);
                        break;
                    case 18:
                        if (this.previousNetworkId == null) {
                            this.previousNetworkId = new NetworkId();
                        }
                        input.readMessage(this.previousNetworkId);
                        break;
                    case 24:
                        int value = input.readInt32();
                        switch (value) {
                            case 0:
                            case 1:
                            case 2:
                            case 3:
                                this.previousNetworkIpSupport = value;
                                break;
                            default:
                                break;
                        }
                    case 32:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 32);
                        i = this.transportTypes == null ? 0 : this.transportTypes.length;
                        newArray = new int[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.transportTypes, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = input.readInt32();
                            input.readTag();
                            i++;
                        }
                        newArray[i] = input.readInt32();
                        this.transportTypes = newArray;
                        break;
                    case 34:
                        int limit = input.pushLimit(input.readRawVarint32());
                        arrayLength = 0;
                        int startPos = input.getPosition();
                        while (input.getBytesUntilLimit() > 0) {
                            input.readInt32();
                            arrayLength++;
                        }
                        input.rewindToPosition(startPos);
                        i = this.transportTypes == null ? 0 : this.transportTypes.length;
                        newArray = new int[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.transportTypes, 0, newArray, 0, i);
                        }
                        while (i < newArray.length) {
                            newArray[i] = input.readInt32();
                            i++;
                        }
                        this.transportTypes = newArray;
                        input.popLimit(limit);
                        break;
                    default:
                        if (WireFormatNano.parseUnknownField(input, tag)) {
                            break;
                        }
                        return this;
                }
            }
        }

        public static DefaultNetworkEvent parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (DefaultNetworkEvent) MessageNano.mergeFrom(new DefaultNetworkEvent(), data);
        }

        public static DefaultNetworkEvent parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new DefaultNetworkEvent().mergeFrom(input);
        }
    }

    public static final class IpConnectivityEvent extends MessageNano {
        private static volatile IpConnectivityEvent[] _emptyArray;
        public ApfProgramEvent apfProgramEvent;
        public ApfStatistics apfStatistics;
        public DefaultNetworkEvent defaultNetworkEvent;
        public DHCPEvent dhcpEvent;
        public DNSLookupBatch dnsLookupBatch;
        public IpProvisioningEvent ipProvisioningEvent;
        public IpReachabilityEvent ipReachabilityEvent;
        public NetworkEvent networkEvent;
        public RaEvent raEvent;
        public long timeMs;
        public ValidationProbeEvent validationProbeEvent;

        public static IpConnectivityEvent[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new IpConnectivityEvent[0];
                    }
                }
            }
            return _emptyArray;
        }

        public IpConnectivityEvent() {
            clear();
        }

        public IpConnectivityEvent clear() {
            this.timeMs = 0;
            this.defaultNetworkEvent = null;
            this.ipReachabilityEvent = null;
            this.networkEvent = null;
            this.dnsLookupBatch = null;
            this.dhcpEvent = null;
            this.ipProvisioningEvent = null;
            this.validationProbeEvent = null;
            this.apfProgramEvent = null;
            this.apfStatistics = null;
            this.raEvent = null;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.timeMs != 0) {
                output.writeInt64(1, this.timeMs);
            }
            if (this.defaultNetworkEvent != null) {
                output.writeMessage(2, this.defaultNetworkEvent);
            }
            if (this.ipReachabilityEvent != null) {
                output.writeMessage(3, this.ipReachabilityEvent);
            }
            if (this.networkEvent != null) {
                output.writeMessage(4, this.networkEvent);
            }
            if (this.dnsLookupBatch != null) {
                output.writeMessage(5, this.dnsLookupBatch);
            }
            if (this.dhcpEvent != null) {
                output.writeMessage(6, this.dhcpEvent);
            }
            if (this.ipProvisioningEvent != null) {
                output.writeMessage(7, this.ipProvisioningEvent);
            }
            if (this.validationProbeEvent != null) {
                output.writeMessage(8, this.validationProbeEvent);
            }
            if (this.apfProgramEvent != null) {
                output.writeMessage(9, this.apfProgramEvent);
            }
            if (this.apfStatistics != null) {
                output.writeMessage(10, this.apfStatistics);
            }
            if (this.raEvent != null) {
                output.writeMessage(11, this.raEvent);
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (this.timeMs != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(1, this.timeMs);
            }
            if (this.defaultNetworkEvent != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(2, this.defaultNetworkEvent);
            }
            if (this.ipReachabilityEvent != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(3, this.ipReachabilityEvent);
            }
            if (this.networkEvent != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(4, this.networkEvent);
            }
            if (this.dnsLookupBatch != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(5, this.dnsLookupBatch);
            }
            if (this.dhcpEvent != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(6, this.dhcpEvent);
            }
            if (this.ipProvisioningEvent != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(7, this.ipProvisioningEvent);
            }
            if (this.validationProbeEvent != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(8, this.validationProbeEvent);
            }
            if (this.apfProgramEvent != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(9, this.apfProgramEvent);
            }
            if (this.apfStatistics != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(10, this.apfStatistics);
            }
            if (this.raEvent != null) {
                return size + CodedOutputByteBufferNano.computeMessageSize(11, this.raEvent);
            }
            return size;
        }

        public IpConnectivityEvent mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case 8:
                        this.timeMs = input.readInt64();
                        break;
                    case 18:
                        if (this.defaultNetworkEvent == null) {
                            this.defaultNetworkEvent = new DefaultNetworkEvent();
                        }
                        input.readMessage(this.defaultNetworkEvent);
                        break;
                    case H.DO_ANIMATION_CALLBACK /*26*/:
                        if (this.ipReachabilityEvent == null) {
                            this.ipReachabilityEvent = new IpReachabilityEvent();
                        }
                        input.readMessage(this.ipReachabilityEvent);
                        break;
                    case 34:
                        if (this.networkEvent == null) {
                            this.networkEvent = new NetworkEvent();
                        }
                        input.readMessage(this.networkEvent);
                        break;
                    case 42:
                        if (this.dnsLookupBatch == null) {
                            this.dnsLookupBatch = new DNSLookupBatch();
                        }
                        input.readMessage(this.dnsLookupBatch);
                        break;
                    case 50:
                        if (this.dhcpEvent == null) {
                            this.dhcpEvent = new DHCPEvent();
                        }
                        input.readMessage(this.dhcpEvent);
                        break;
                    case 58:
                        if (this.ipProvisioningEvent == null) {
                            this.ipProvisioningEvent = new IpProvisioningEvent();
                        }
                        input.readMessage(this.ipProvisioningEvent);
                        break;
                    case HdmiCecKeycode.CEC_KEYCODE_VOLUME_DOWN /*66*/:
                        if (this.validationProbeEvent == null) {
                            this.validationProbeEvent = new ValidationProbeEvent();
                        }
                        input.readMessage(this.validationProbeEvent);
                        break;
                    case HdmiCecKeycode.CEC_KEYCODE_EJECT /*74*/:
                        if (this.apfProgramEvent == null) {
                            this.apfProgramEvent = new ApfProgramEvent();
                        }
                        input.readMessage(this.apfProgramEvent);
                        break;
                    case HdmiCecKeycode.CEC_KEYCODE_VIDEO_ON_DEMAND /*82*/:
                        if (this.apfStatistics == null) {
                            this.apfStatistics = new ApfStatistics();
                        }
                        input.readMessage(this.apfStatistics);
                        break;
                    case 90:
                        if (this.raEvent == null) {
                            this.raEvent = new RaEvent();
                        }
                        input.readMessage(this.raEvent);
                        break;
                    default:
                        if (WireFormatNano.parseUnknownField(input, tag)) {
                            break;
                        }
                        return this;
                }
            }
        }

        public static IpConnectivityEvent parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (IpConnectivityEvent) MessageNano.mergeFrom(new IpConnectivityEvent(), data);
        }

        public static IpConnectivityEvent parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new IpConnectivityEvent().mergeFrom(input);
        }
    }

    public static final class IpConnectivityLog extends MessageNano {
        private static volatile IpConnectivityLog[] _emptyArray;
        public int droppedEvents;
        public IpConnectivityEvent[] events;

        public static IpConnectivityLog[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new IpConnectivityLog[0];
                    }
                }
            }
            return _emptyArray;
        }

        public IpConnectivityLog() {
            clear();
        }

        public IpConnectivityLog clear() {
            this.events = IpConnectivityEvent.emptyArray();
            this.droppedEvents = 0;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.events != null && this.events.length > 0) {
                for (IpConnectivityEvent element : this.events) {
                    if (element != null) {
                        output.writeMessage(1, element);
                    }
                }
            }
            if (this.droppedEvents != 0) {
                output.writeInt32(2, this.droppedEvents);
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (this.events != null && this.events.length > 0) {
                for (IpConnectivityEvent element : this.events) {
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(1, element);
                    }
                }
            }
            if (this.droppedEvents != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(2, this.droppedEvents);
            }
            return size;
        }

        public IpConnectivityLog mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case 10:
                        int i;
                        int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 10);
                        if (this.events == null) {
                            i = 0;
                        } else {
                            i = this.events.length;
                        }
                        IpConnectivityEvent[] newArray = new IpConnectivityEvent[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.events, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = new IpConnectivityEvent();
                            input.readMessage(newArray[i]);
                            input.readTag();
                            i++;
                        }
                        newArray[i] = new IpConnectivityEvent();
                        input.readMessage(newArray[i]);
                        this.events = newArray;
                        break;
                    case 16:
                        this.droppedEvents = input.readInt32();
                        break;
                    default:
                        if (WireFormatNano.parseUnknownField(input, tag)) {
                            break;
                        }
                        return this;
                }
            }
        }

        public static IpConnectivityLog parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (IpConnectivityLog) MessageNano.mergeFrom(new IpConnectivityLog(), data);
        }

        public static IpConnectivityLog parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new IpConnectivityLog().mergeFrom(input);
        }
    }

    public static final class IpProvisioningEvent extends MessageNano {
        private static volatile IpProvisioningEvent[] _emptyArray;
        public int eventType;
        public String ifName;
        public int latencyMs;

        public static IpProvisioningEvent[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new IpProvisioningEvent[0];
                    }
                }
            }
            return _emptyArray;
        }

        public IpProvisioningEvent() {
            clear();
        }

        public IpProvisioningEvent clear() {
            this.ifName = IElsaManager.EMPTY_PACKAGE;
            this.eventType = 0;
            this.latencyMs = 0;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (!this.ifName.equals(IElsaManager.EMPTY_PACKAGE)) {
                output.writeString(1, this.ifName);
            }
            if (this.eventType != 0) {
                output.writeInt32(2, this.eventType);
            }
            if (this.latencyMs != 0) {
                output.writeInt32(3, this.latencyMs);
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (!this.ifName.equals(IElsaManager.EMPTY_PACKAGE)) {
                size += CodedOutputByteBufferNano.computeStringSize(1, this.ifName);
            }
            if (this.eventType != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, this.eventType);
            }
            if (this.latencyMs != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(3, this.latencyMs);
            }
            return size;
        }

        public IpProvisioningEvent mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case 10:
                        this.ifName = input.readString();
                        break;
                    case 16:
                        this.eventType = input.readInt32();
                        break;
                    case 24:
                        this.latencyMs = input.readInt32();
                        break;
                    default:
                        if (WireFormatNano.parseUnknownField(input, tag)) {
                            break;
                        }
                        return this;
                }
            }
        }

        public static IpProvisioningEvent parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (IpProvisioningEvent) MessageNano.mergeFrom(new IpProvisioningEvent(), data);
        }

        public static IpProvisioningEvent parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new IpProvisioningEvent().mergeFrom(input);
        }
    }

    public static final class IpReachabilityEvent extends MessageNano {
        private static volatile IpReachabilityEvent[] _emptyArray;
        public int eventType;
        public String ifName;

        public static IpReachabilityEvent[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new IpReachabilityEvent[0];
                    }
                }
            }
            return _emptyArray;
        }

        public IpReachabilityEvent() {
            clear();
        }

        public IpReachabilityEvent clear() {
            this.ifName = IElsaManager.EMPTY_PACKAGE;
            this.eventType = 0;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (!this.ifName.equals(IElsaManager.EMPTY_PACKAGE)) {
                output.writeString(1, this.ifName);
            }
            if (this.eventType != 0) {
                output.writeInt32(2, this.eventType);
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (!this.ifName.equals(IElsaManager.EMPTY_PACKAGE)) {
                size += CodedOutputByteBufferNano.computeStringSize(1, this.ifName);
            }
            if (this.eventType != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(2, this.eventType);
            }
            return size;
        }

        public IpReachabilityEvent mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case 10:
                        this.ifName = input.readString();
                        break;
                    case 16:
                        this.eventType = input.readInt32();
                        break;
                    default:
                        if (WireFormatNano.parseUnknownField(input, tag)) {
                            break;
                        }
                        return this;
                }
            }
        }

        public static IpReachabilityEvent parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (IpReachabilityEvent) MessageNano.mergeFrom(new IpReachabilityEvent(), data);
        }

        public static IpReachabilityEvent parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new IpReachabilityEvent().mergeFrom(input);
        }
    }

    public static final class NetworkEvent extends MessageNano {
        private static volatile NetworkEvent[] _emptyArray;
        public int eventType;
        public int latencyMs;
        public NetworkId networkId;

        public static NetworkEvent[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new NetworkEvent[0];
                    }
                }
            }
            return _emptyArray;
        }

        public NetworkEvent() {
            clear();
        }

        public NetworkEvent clear() {
            this.networkId = null;
            this.eventType = 0;
            this.latencyMs = 0;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.networkId != null) {
                output.writeMessage(1, this.networkId);
            }
            if (this.eventType != 0) {
                output.writeInt32(2, this.eventType);
            }
            if (this.latencyMs != 0) {
                output.writeInt32(3, this.latencyMs);
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (this.networkId != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(1, this.networkId);
            }
            if (this.eventType != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, this.eventType);
            }
            if (this.latencyMs != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(3, this.latencyMs);
            }
            return size;
        }

        public NetworkEvent mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case 10:
                        if (this.networkId == null) {
                            this.networkId = new NetworkId();
                        }
                        input.readMessage(this.networkId);
                        break;
                    case 16:
                        this.eventType = input.readInt32();
                        break;
                    case 24:
                        this.latencyMs = input.readInt32();
                        break;
                    default:
                        if (WireFormatNano.parseUnknownField(input, tag)) {
                            break;
                        }
                        return this;
                }
            }
        }

        public static NetworkEvent parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (NetworkEvent) MessageNano.mergeFrom(new NetworkEvent(), data);
        }

        public static NetworkEvent parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new NetworkEvent().mergeFrom(input);
        }
    }

    public static final class NetworkId extends MessageNano {
        private static volatile NetworkId[] _emptyArray;
        public int networkId;

        public static NetworkId[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new NetworkId[0];
                    }
                }
            }
            return _emptyArray;
        }

        public NetworkId() {
            clear();
        }

        public NetworkId clear() {
            this.networkId = 0;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.networkId != 0) {
                output.writeInt32(1, this.networkId);
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (this.networkId != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(1, this.networkId);
            }
            return size;
        }

        public NetworkId mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case 8:
                        this.networkId = input.readInt32();
                        break;
                    default:
                        if (WireFormatNano.parseUnknownField(input, tag)) {
                            break;
                        }
                        return this;
                }
            }
        }

        public static NetworkId parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (NetworkId) MessageNano.mergeFrom(new NetworkId(), data);
        }

        public static NetworkId parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new NetworkId().mergeFrom(input);
        }
    }

    public static final class RaEvent extends MessageNano {
        private static volatile RaEvent[] _emptyArray;
        public long dnsslLifetime;
        public long prefixPreferredLifetime;
        public long prefixValidLifetime;
        public long rdnssLifetime;
        public long routeInfoLifetime;
        public long routerLifetime;

        public static RaEvent[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new RaEvent[0];
                    }
                }
            }
            return _emptyArray;
        }

        public RaEvent() {
            clear();
        }

        public RaEvent clear() {
            this.routerLifetime = 0;
            this.prefixValidLifetime = 0;
            this.prefixPreferredLifetime = 0;
            this.routeInfoLifetime = 0;
            this.rdnssLifetime = 0;
            this.dnsslLifetime = 0;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.routerLifetime != 0) {
                output.writeInt64(1, this.routerLifetime);
            }
            if (this.prefixValidLifetime != 0) {
                output.writeInt64(2, this.prefixValidLifetime);
            }
            if (this.prefixPreferredLifetime != 0) {
                output.writeInt64(3, this.prefixPreferredLifetime);
            }
            if (this.routeInfoLifetime != 0) {
                output.writeInt64(4, this.routeInfoLifetime);
            }
            if (this.rdnssLifetime != 0) {
                output.writeInt64(5, this.rdnssLifetime);
            }
            if (this.dnsslLifetime != 0) {
                output.writeInt64(6, this.dnsslLifetime);
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (this.routerLifetime != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(1, this.routerLifetime);
            }
            if (this.prefixValidLifetime != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(2, this.prefixValidLifetime);
            }
            if (this.prefixPreferredLifetime != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(3, this.prefixPreferredLifetime);
            }
            if (this.routeInfoLifetime != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(4, this.routeInfoLifetime);
            }
            if (this.rdnssLifetime != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(5, this.rdnssLifetime);
            }
            if (this.dnsslLifetime != 0) {
                return size + CodedOutputByteBufferNano.computeInt64Size(6, this.dnsslLifetime);
            }
            return size;
        }

        public RaEvent mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case 8:
                        this.routerLifetime = input.readInt64();
                        break;
                    case 16:
                        this.prefixValidLifetime = input.readInt64();
                        break;
                    case 24:
                        this.prefixPreferredLifetime = input.readInt64();
                        break;
                    case 32:
                        this.routeInfoLifetime = input.readInt64();
                        break;
                    case 40:
                        this.rdnssLifetime = input.readInt64();
                        break;
                    case 48:
                        this.dnsslLifetime = input.readInt64();
                        break;
                    default:
                        if (WireFormatNano.parseUnknownField(input, tag)) {
                            break;
                        }
                        return this;
                }
            }
        }

        public static RaEvent parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (RaEvent) MessageNano.mergeFrom(new RaEvent(), data);
        }

        public static RaEvent parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new RaEvent().mergeFrom(input);
        }
    }

    public static final class ValidationProbeEvent extends MessageNano {
        private static volatile ValidationProbeEvent[] _emptyArray;
        public int latencyMs;
        public NetworkId networkId;
        public int probeResult;
        public int probeType;

        public static ValidationProbeEvent[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new ValidationProbeEvent[0];
                    }
                }
            }
            return _emptyArray;
        }

        public ValidationProbeEvent() {
            clear();
        }

        public ValidationProbeEvent clear() {
            this.networkId = null;
            this.latencyMs = 0;
            this.probeType = 0;
            this.probeResult = 0;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.networkId != null) {
                output.writeMessage(1, this.networkId);
            }
            if (this.latencyMs != 0) {
                output.writeInt32(2, this.latencyMs);
            }
            if (this.probeType != 0) {
                output.writeInt32(3, this.probeType);
            }
            if (this.probeResult != 0) {
                output.writeInt32(4, this.probeResult);
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (this.networkId != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(1, this.networkId);
            }
            if (this.latencyMs != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, this.latencyMs);
            }
            if (this.probeType != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, this.probeType);
            }
            if (this.probeResult != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(4, this.probeResult);
            }
            return size;
        }

        public ValidationProbeEvent mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case 10:
                        if (this.networkId == null) {
                            this.networkId = new NetworkId();
                        }
                        input.readMessage(this.networkId);
                        break;
                    case 16:
                        this.latencyMs = input.readInt32();
                        break;
                    case 24:
                        this.probeType = input.readInt32();
                        break;
                    case 32:
                        this.probeResult = input.readInt32();
                        break;
                    default:
                        if (WireFormatNano.parseUnknownField(input, tag)) {
                            break;
                        }
                        return this;
                }
            }
        }

        public static ValidationProbeEvent parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (ValidationProbeEvent) MessageNano.mergeFrom(new ValidationProbeEvent(), data);
        }

        public static ValidationProbeEvent parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new ValidationProbeEvent().mergeFrom(input);
        }
    }
}
