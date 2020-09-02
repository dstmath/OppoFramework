package com.android.framework.protobuf;

import com.android.framework.protobuf.AbstractMessageLite;
import com.android.framework.protobuf.MessageLite;
import java.io.IOException;
import java.io.InputStream;

public abstract class AbstractParser<MessageType extends MessageLite> implements Parser<MessageType> {
    private static final ExtensionRegistryLite EMPTY_REGISTRY = ExtensionRegistryLite.getEmptyRegistry();

    private UninitializedMessageException newUninitializedMessageException(MessageType message) {
        if (message instanceof AbstractMessageLite) {
            return message.newUninitializedMessageException();
        }
        return new UninitializedMessageException((MessageLite) message);
    }

    private MessageType checkMessageInitialized(MessageType message) throws InvalidProtocolBufferException {
        if (message == null || message.isInitialized()) {
            return message;
        }
        throw newUninitializedMessageException(message).asInvalidProtocolBufferException().setUnfinishedMessage(message);
    }

    @Override // com.android.framework.protobuf.Parser
    public MessageType parsePartialFrom(CodedInputStream input) throws InvalidProtocolBufferException {
        return parsePartialFrom(input, EMPTY_REGISTRY);
    }

    @Override // com.android.framework.protobuf.Parser
    public MessageType parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
        return checkMessageInitialized(parsePartialFrom(input, extensionRegistry));
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: MutableMD:(com.android.framework.protobuf.CodedInputStream, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException
     arg types: [com.android.framework.protobuf.CodedInputStream, com.android.framework.protobuf.ExtensionRegistryLite]
     candidates:
      MutableMD:(com.android.framework.protobuf.ByteString, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(byte[], com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException
      com.android.framework.protobuf.AbstractParser.parseFrom(com.android.framework.protobuf.ByteString, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object
      com.android.framework.protobuf.AbstractParser.parseFrom(com.android.framework.protobuf.CodedInputStream, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object
      com.android.framework.protobuf.AbstractParser.parseFrom(java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object
      com.android.framework.protobuf.AbstractParser.parseFrom(byte[], com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object
      MutableMD:(com.android.framework.protobuf.ByteString, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(com.android.framework.protobuf.CodedInputStream, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(byte[], com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(com.android.framework.protobuf.CodedInputStream, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException */
    @Override // com.android.framework.protobuf.Parser
    public MessageType parseFrom(CodedInputStream input) throws InvalidProtocolBufferException {
        return parseFrom(input, EMPTY_REGISTRY);
    }

    @Override // com.android.framework.protobuf.Parser
    public MessageType parsePartialFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
        try {
            CodedInputStream input = data.newCodedInput();
            MessageType message = parsePartialFrom(input, extensionRegistry);
            try {
                input.checkLastTagWas(0);
                return message;
            } catch (InvalidProtocolBufferException e) {
                throw e.setUnfinishedMessage(message);
            }
        } catch (InvalidProtocolBufferException e2) {
            throw e2;
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: MutableMD:(com.android.framework.protobuf.ByteString, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException
     arg types: [com.android.framework.protobuf.ByteString, com.android.framework.protobuf.ExtensionRegistryLite]
     candidates:
      MutableMD:(java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(byte[], com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException
      com.android.framework.protobuf.AbstractParser.parsePartialFrom(com.android.framework.protobuf.ByteString, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object
      com.android.framework.protobuf.AbstractParser.parsePartialFrom(java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object
      com.android.framework.protobuf.AbstractParser.parsePartialFrom(byte[], com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object
      MutableMD:(com.android.framework.protobuf.ByteString, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(com.android.framework.protobuf.CodedInputStream, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(byte[], com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(com.android.framework.protobuf.ByteString, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException */
    @Override // com.android.framework.protobuf.Parser
    public MessageType parsePartialFrom(ByteString data) throws InvalidProtocolBufferException {
        return parsePartialFrom(data, EMPTY_REGISTRY);
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: MutableMD:(com.android.framework.protobuf.ByteString, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException
     arg types: [com.android.framework.protobuf.ByteString, com.android.framework.protobuf.ExtensionRegistryLite]
     candidates:
      MutableMD:(java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(byte[], com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException
      com.android.framework.protobuf.AbstractParser.parsePartialFrom(com.android.framework.protobuf.ByteString, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object
      com.android.framework.protobuf.AbstractParser.parsePartialFrom(java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object
      com.android.framework.protobuf.AbstractParser.parsePartialFrom(byte[], com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object
      MutableMD:(com.android.framework.protobuf.ByteString, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(com.android.framework.protobuf.CodedInputStream, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(byte[], com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(com.android.framework.protobuf.ByteString, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException */
    @Override // com.android.framework.protobuf.Parser
    public MessageType parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
        return checkMessageInitialized(parsePartialFrom(data, extensionRegistry));
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: MutableMD:(com.android.framework.protobuf.ByteString, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException
     arg types: [com.android.framework.protobuf.ByteString, com.android.framework.protobuf.ExtensionRegistryLite]
     candidates:
      MutableMD:(com.android.framework.protobuf.CodedInputStream, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(byte[], com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException
      com.android.framework.protobuf.AbstractParser.parseFrom(com.android.framework.protobuf.ByteString, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object
      com.android.framework.protobuf.AbstractParser.parseFrom(com.android.framework.protobuf.CodedInputStream, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object
      com.android.framework.protobuf.AbstractParser.parseFrom(java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object
      com.android.framework.protobuf.AbstractParser.parseFrom(byte[], com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object
      MutableMD:(com.android.framework.protobuf.ByteString, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(com.android.framework.protobuf.CodedInputStream, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(byte[], com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(com.android.framework.protobuf.ByteString, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException */
    @Override // com.android.framework.protobuf.Parser
    public MessageType parseFrom(ByteString data) throws InvalidProtocolBufferException {
        return parseFrom(data, EMPTY_REGISTRY);
    }

    @Override // com.android.framework.protobuf.Parser
    public MessageType parsePartialFrom(byte[] data, int off, int len, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
        try {
            CodedInputStream input = CodedInputStream.newInstance(data, off, len);
            MessageType message = parsePartialFrom(input, extensionRegistry);
            try {
                input.checkLastTagWas(0);
                return message;
            } catch (InvalidProtocolBufferException e) {
                throw e.setUnfinishedMessage(message);
            }
        } catch (InvalidProtocolBufferException e2) {
            throw e2;
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: MutableMD:(byte[], int, int, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException
     arg types: [byte[], int, int, com.android.framework.protobuf.ExtensionRegistryLite]
     candidates:
      com.android.framework.protobuf.AbstractParser.parsePartialFrom(byte[], int, int, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object
      MutableMD:(byte[], int, int, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(byte[], int, int, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException */
    @Override // com.android.framework.protobuf.Parser
    public MessageType parsePartialFrom(byte[] data, int off, int len) throws InvalidProtocolBufferException {
        return parsePartialFrom(data, off, len, EMPTY_REGISTRY);
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: MutableMD:(byte[], int, int, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException
     arg types: [byte[], int, int, com.android.framework.protobuf.ExtensionRegistryLite]
     candidates:
      com.android.framework.protobuf.AbstractParser.parsePartialFrom(byte[], int, int, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object
      MutableMD:(byte[], int, int, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(byte[], int, int, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException */
    @Override // com.android.framework.protobuf.Parser
    public MessageType parsePartialFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
        return parsePartialFrom(data, 0, data.length, extensionRegistry);
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: MutableMD:(byte[], int, int, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException
     arg types: [byte[], int, int, com.android.framework.protobuf.ExtensionRegistryLite]
     candidates:
      com.android.framework.protobuf.AbstractParser.parsePartialFrom(byte[], int, int, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object
      MutableMD:(byte[], int, int, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(byte[], int, int, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException */
    @Override // com.android.framework.protobuf.Parser
    public MessageType parsePartialFrom(byte[] data) throws InvalidProtocolBufferException {
        return parsePartialFrom(data, 0, data.length, EMPTY_REGISTRY);
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: MutableMD:(byte[], int, int, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException
     arg types: [byte[], int, int, com.android.framework.protobuf.ExtensionRegistryLite]
     candidates:
      com.android.framework.protobuf.AbstractParser.parsePartialFrom(byte[], int, int, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object
      MutableMD:(byte[], int, int, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(byte[], int, int, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException */
    @Override // com.android.framework.protobuf.Parser
    public MessageType parseFrom(byte[] data, int off, int len, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
        return checkMessageInitialized(parsePartialFrom(data, off, len, extensionRegistry));
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: MutableMD:(byte[], int, int, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException
     arg types: [byte[], int, int, com.android.framework.protobuf.ExtensionRegistryLite]
     candidates:
      com.android.framework.protobuf.AbstractParser.parseFrom(byte[], int, int, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object
      MutableMD:(byte[], int, int, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(byte[], int, int, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException */
    @Override // com.android.framework.protobuf.Parser
    public MessageType parseFrom(byte[] data, int off, int len) throws InvalidProtocolBufferException {
        return parseFrom(data, off, len, EMPTY_REGISTRY);
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: MutableMD:(byte[], int, int, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException
     arg types: [byte[], int, int, com.android.framework.protobuf.ExtensionRegistryLite]
     candidates:
      com.android.framework.protobuf.AbstractParser.parseFrom(byte[], int, int, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object
      MutableMD:(byte[], int, int, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(byte[], int, int, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException */
    @Override // com.android.framework.protobuf.Parser
    public MessageType parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
        return parseFrom(data, 0, data.length, extensionRegistry);
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: MutableMD:(byte[], com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException
     arg types: [byte[], com.android.framework.protobuf.ExtensionRegistryLite]
     candidates:
      MutableMD:(com.android.framework.protobuf.ByteString, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(com.android.framework.protobuf.CodedInputStream, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException
      com.android.framework.protobuf.AbstractParser.parseFrom(com.android.framework.protobuf.ByteString, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object
      com.android.framework.protobuf.AbstractParser.parseFrom(com.android.framework.protobuf.CodedInputStream, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object
      com.android.framework.protobuf.AbstractParser.parseFrom(java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object
      com.android.framework.protobuf.AbstractParser.parseFrom(byte[], com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object
      MutableMD:(com.android.framework.protobuf.ByteString, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(com.android.framework.protobuf.CodedInputStream, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(byte[], com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(byte[], com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException */
    @Override // com.android.framework.protobuf.Parser
    public MessageType parseFrom(byte[] data) throws InvalidProtocolBufferException {
        return parseFrom(data, EMPTY_REGISTRY);
    }

    @Override // com.android.framework.protobuf.Parser
    public MessageType parsePartialFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
        CodedInputStream codedInput = CodedInputStream.newInstance(input);
        MessageType message = parsePartialFrom(codedInput, extensionRegistry);
        try {
            codedInput.checkLastTagWas(0);
            return message;
        } catch (InvalidProtocolBufferException e) {
            throw e.setUnfinishedMessage(message);
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: MutableMD:(java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException
     arg types: [java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite]
     candidates:
      MutableMD:(com.android.framework.protobuf.ByteString, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(byte[], com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException
      com.android.framework.protobuf.AbstractParser.parsePartialFrom(com.android.framework.protobuf.ByteString, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object
      com.android.framework.protobuf.AbstractParser.parsePartialFrom(java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object
      com.android.framework.protobuf.AbstractParser.parsePartialFrom(byte[], com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object
      MutableMD:(com.android.framework.protobuf.ByteString, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(com.android.framework.protobuf.CodedInputStream, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(byte[], com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException */
    @Override // com.android.framework.protobuf.Parser
    public MessageType parsePartialFrom(InputStream input) throws InvalidProtocolBufferException {
        return parsePartialFrom(input, EMPTY_REGISTRY);
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: MutableMD:(java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException
     arg types: [java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite]
     candidates:
      MutableMD:(com.android.framework.protobuf.ByteString, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(byte[], com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException
      com.android.framework.protobuf.AbstractParser.parsePartialFrom(com.android.framework.protobuf.ByteString, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object
      com.android.framework.protobuf.AbstractParser.parsePartialFrom(java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object
      com.android.framework.protobuf.AbstractParser.parsePartialFrom(byte[], com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object
      MutableMD:(com.android.framework.protobuf.ByteString, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(com.android.framework.protobuf.CodedInputStream, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(byte[], com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException */
    @Override // com.android.framework.protobuf.Parser
    public MessageType parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
        return checkMessageInitialized(parsePartialFrom(input, extensionRegistry));
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: MutableMD:(java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException
     arg types: [java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite]
     candidates:
      MutableMD:(com.android.framework.protobuf.ByteString, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(com.android.framework.protobuf.CodedInputStream, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(byte[], com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException
      com.android.framework.protobuf.AbstractParser.parseFrom(com.android.framework.protobuf.ByteString, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object
      com.android.framework.protobuf.AbstractParser.parseFrom(com.android.framework.protobuf.CodedInputStream, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object
      com.android.framework.protobuf.AbstractParser.parseFrom(java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object
      com.android.framework.protobuf.AbstractParser.parseFrom(byte[], com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object
      MutableMD:(com.android.framework.protobuf.ByteString, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(com.android.framework.protobuf.CodedInputStream, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(byte[], com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException */
    @Override // com.android.framework.protobuf.Parser
    public MessageType parseFrom(InputStream input) throws InvalidProtocolBufferException {
        return parseFrom(input, EMPTY_REGISTRY);
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: MutableMD:(java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException
     arg types: [java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite]
     candidates:
      MutableMD:(com.android.framework.protobuf.ByteString, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(byte[], com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException
      com.android.framework.protobuf.AbstractParser.parsePartialFrom(com.android.framework.protobuf.ByteString, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object
      com.android.framework.protobuf.AbstractParser.parsePartialFrom(java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object
      com.android.framework.protobuf.AbstractParser.parsePartialFrom(byte[], com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object
      MutableMD:(com.android.framework.protobuf.ByteString, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(com.android.framework.protobuf.CodedInputStream, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(byte[], com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException */
    @Override // com.android.framework.protobuf.Parser
    public MessageType parsePartialDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
        try {
            int firstByte = input.read();
            if (firstByte == -1) {
                return null;
            }
            return parsePartialFrom(new AbstractMessageLite.Builder.LimitedInputStream(input, CodedInputStream.readRawVarint32(firstByte, input)), extensionRegistry);
        } catch (IOException e) {
            throw new InvalidProtocolBufferException(e.getMessage());
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: MutableMD:(java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException
     arg types: [java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite]
     candidates:
      com.android.framework.protobuf.AbstractParser.parsePartialDelimitedFrom(java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object
      MutableMD:(java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException */
    @Override // com.android.framework.protobuf.Parser
    public MessageType parsePartialDelimitedFrom(InputStream input) throws InvalidProtocolBufferException {
        return parsePartialDelimitedFrom(input, EMPTY_REGISTRY);
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: MutableMD:(java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException
     arg types: [java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite]
     candidates:
      com.android.framework.protobuf.AbstractParser.parsePartialDelimitedFrom(java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object
      MutableMD:(java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException */
    @Override // com.android.framework.protobuf.Parser
    public MessageType parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
        return checkMessageInitialized(parsePartialDelimitedFrom(input, extensionRegistry));
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: MutableMD:(java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException
     arg types: [java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite]
     candidates:
      com.android.framework.protobuf.AbstractParser.parseDelimitedFrom(java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object
      MutableMD:(java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite):java.lang.Object throws com.android.framework.protobuf.InvalidProtocolBufferException
      MutableMD:(java.io.InputStream, com.android.framework.protobuf.ExtensionRegistryLite):com.android.framework.protobuf.MessageLite throws com.android.framework.protobuf.InvalidProtocolBufferException */
    @Override // com.android.framework.protobuf.Parser
    public MessageType parseDelimitedFrom(InputStream input) throws InvalidProtocolBufferException {
        return parseDelimitedFrom(input, EMPTY_REGISTRY);
    }
}
