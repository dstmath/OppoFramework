package com.android.server.connectivity.networkrecovery.dnsresolve;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Header implements MessageContent<Header> {
    private short additionalRecords;
    private short answerEntries;
    private boolean authoritativeAnswer;
    private short authorityRecords;
    private boolean availableFlag = true;
    private short id;
    private Opcode opcode;
    private short questionEntries;
    private boolean recursionAvailable;
    private boolean recursionDesired;
    private boolean request;
    private ResponseCode responseCode;
    private boolean truncated;

    public boolean isAvailable() {
        return this.availableFlag;
    }

    public short getId() {
        return this.id;
    }

    public void setId(short id2) {
        this.id = id2;
    }

    public boolean isRequest() {
        return this.request;
    }

    public void setRequest(boolean request2) {
        this.request = request2;
    }

    public Opcode getOpcode() {
        return this.opcode;
    }

    public void setOpcode(Opcode opcode2) {
        this.opcode = opcode2;
    }

    public boolean isAuthoritativeAnswer() {
        return this.authoritativeAnswer;
    }

    public void setAuthoritativeAnswer(boolean authoritativeAnswer2) {
        this.authoritativeAnswer = authoritativeAnswer2;
    }

    public boolean isTruncated() {
        return this.truncated;
    }

    public void setTruncated(boolean truncated2) {
        this.truncated = truncated2;
    }

    public boolean isRecursionDesired() {
        return this.recursionDesired;
    }

    public void setRecursionDesired(boolean recursionDesired2) {
        this.recursionDesired = recursionDesired2;
    }

    public boolean isRecursionAvailable() {
        return this.recursionAvailable;
    }

    public void setRecursionAvailable(boolean recursionAvailable2) {
        this.recursionAvailable = recursionAvailable2;
    }

    public ResponseCode getResponseCode() {
        return this.responseCode;
    }

    public void setResponseCode(ResponseCode responseCode2) {
        this.responseCode = responseCode2;
    }

    public int getQuestionEntries() {
        return this.questionEntries;
    }

    public void setQuestionEntries(short questionEntries2) {
        this.questionEntries = questionEntries2;
    }

    public int getAnswerEntries() {
        return this.answerEntries;
    }

    public void setAnswerEntries(short answerEntries2) {
        this.answerEntries = answerEntries2;
    }

    public int getAuthorityRecords() {
        return this.authorityRecords;
    }

    public void setAuthorityRecords(short authorityRecords2) {
        this.authorityRecords = authorityRecords2;
    }

    public int getAdditionalRecords() {
        return this.additionalRecords;
    }

    public void setAdditionalRecords(short additionalRecords2) {
        this.additionalRecords = additionalRecords2;
    }

    @Override // com.android.server.connectivity.networkrecovery.dnsresolve.MessageContent
    public Header toBytes(ByteBuffer buf) {
        buf.putShort(this.id);
        buf.putShort((short) (((this.recursionDesired ? 1 : 0) << 8) | ((short) (((this.opcode.getCode() & 15) << 11) | ((short) ((!this.request ? 1 : 0) << 15))))));
        buf.putShort(this.questionEntries);
        buf.putShort(this.answerEntries);
        buf.putShort(this.authorityRecords);
        buf.putShort(this.additionalRecords);
        return this;
    }

    @Override // com.android.server.connectivity.networkrecovery.dnsresolve.MessageContent
    public Header fromBytes(ByteBuffer buf) throws IOException {
        this.id = buf.getShort();
        int flags = buf.getShort();
        boolean z = true;
        this.request = ((flags >> 15) & 1) == 0;
        this.opcode = Opcode.byCode((flags >> 11) & 15);
        this.authoritativeAnswer = ((flags >> 10) & 1) == 1;
        this.truncated = ((flags >> 9) & 1) == 1;
        this.recursionDesired = ((flags >> 8) & 1) == 1;
        if (((flags >> 7) & 1) != 1) {
            z = false;
        }
        this.recursionAvailable = z;
        this.responseCode = ResponseCode.byCode(flags & 15);
        if (!(this.opcode == Opcode.QUERY && this.responseCode == ResponseCode.NO_ERROR)) {
            this.availableFlag = false;
        }
        this.questionEntries = buf.getShort();
        this.answerEntries = buf.getShort();
        this.authorityRecords = buf.getShort();
        this.additionalRecords = buf.getShort();
        return this;
    }

    static void set16Bit(byte[] target, int source, int targetOffset) {
        target[targetOffset] = (byte) ((source >> 8) & 255);
        target[targetOffset + 1] = (byte) (source & 255);
    }

    static int get16Bit(byte[] source, int offset) {
        return (source[offset] << 8) | source[offset + 1];
    }

    public enum Opcode {
        QUERY(0),
        IQUERY(1),
        STATUS(2),
        INVALID(3);
        
        private final int code;

        private Opcode(int code2) {
            this.code = code2;
        }

        public int getCode() {
            return this.code;
        }

        public static Opcode byCode(int code2) {
            Opcode[] values = values();
            for (Opcode o : values) {
                if (o.code == code2) {
                    return o;
                }
            }
            System.err.println("No Opcode with code " + code2 + " exists.");
            return INVALID;
        }
    }

    public enum ResponseCode {
        NO_ERROR(0),
        FORMAT_ERROR(1),
        SERVER_FAILURE(2),
        NAME_ERROR(3),
        NOT_IMPLEMENTED(4),
        REFUSED(5),
        INVALID(6);
        
        private final int code;

        private ResponseCode(int code2) {
            this.code = code2;
        }

        public int getCode() {
            return this.code;
        }

        public static ResponseCode byCode(int code2) {
            ResponseCode[] values = values();
            for (ResponseCode r : values) {
                if (r.code == code2) {
                    return r;
                }
            }
            System.err.println("No ResponseCode with code " + code2 + " exists.");
            return INVALID;
        }
    }

    public String toString() {
        return "Header [id=" + ((int) this.id) + ", request=" + this.request + ", opcode=" + this.opcode + ", authoritativeAnswer=" + this.authoritativeAnswer + ", truncated=" + this.truncated + ", recursionDesired=" + this.recursionDesired + ", recursionAvailable=" + this.recursionAvailable + ", responseCode=" + this.responseCode + ", questionEntries=" + ((int) this.questionEntries) + ", answerEntries=" + ((int) this.answerEntries) + ", authorityRecords=" + ((int) this.authorityRecords) + ", additionalRecords=" + ((int) this.additionalRecords) + "]";
    }
}
