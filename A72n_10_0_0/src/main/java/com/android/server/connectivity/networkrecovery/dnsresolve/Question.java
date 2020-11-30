package com.android.server.connectivity.networkrecovery.dnsresolve;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class Question implements MessageContent<Question> {
    private Domain domain;
    private RecordClass questionClass;
    private Type questionType;

    public Domain getDomain() {
        return this.domain;
    }

    public void setDomain(Domain domain2) {
        this.domain = domain2;
    }

    public Type getQuestionType() {
        return this.questionType;
    }

    public void setQuestionType(Type questionType2) {
        this.questionType = questionType2;
    }

    public RecordClass getQuestionClass() {
        return this.questionClass;
    }

    public void setQuestionClass(RecordClass questionClass2) {
        this.questionClass = questionClass2;
    }

    @Override // com.android.server.connectivity.networkrecovery.dnsresolve.MessageContent
    public Question toBytes(ByteBuffer buf) throws IOException {
        this.domain.toBytes(buf);
        buf.putShort((short) this.questionType.getCode());
        buf.putShort((short) this.questionClass.getCode());
        return this;
    }

    @Override // com.android.server.connectivity.networkrecovery.dnsresolve.MessageContent
    public Question fromBytes(ByteBuffer buf) throws IOException, BufferUnderflowException {
        this.domain = new Domain().fromBytes(buf);
        try {
            this.questionType = Type.byCode(buf.getShort());
            this.questionClass = RecordClass.byCode(buf.getShort());
            return this;
        } catch (BufferUnderflowException e) {
            throw new BufferUnderflowException();
        }
    }

    public String toString() {
        return "Question [domain=" + this.domain + ", questionType=" + this.questionType + ", questionClass=" + this.questionClass + "]";
    }
}
