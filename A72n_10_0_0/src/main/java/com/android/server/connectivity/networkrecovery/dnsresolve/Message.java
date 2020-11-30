package com.android.server.connectivity.networkrecovery.dnsresolve;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;

public class Message implements MessageContent<Message> {
    private final Collection<Record> additional = new ArrayList();
    private final Collection<Record> answers = new ArrayList();
    private final Collection<Record> authority = new ArrayList();
    public boolean avivableflag = true;
    private Header header;
    private final Collection<Question> questions = new ArrayList();

    public Header getHeader() {
        return this.header;
    }

    public void setHeader(Header header2) {
        this.header = header2;
    }

    public Collection<Question> getQuestions() {
        return this.questions;
    }

    public Collection<Record> getAnswers() {
        return this.answers;
    }

    public Collection<Record> getAuthority() {
        return this.authority;
    }

    public Collection<Record> getAdditional() {
        return this.additional;
    }

    public void setFlag(boolean flag) {
        this.avivableflag = flag;
    }

    @Override // com.android.server.connectivity.networkrecovery.dnsresolve.MessageContent
    public Message toBytes(ByteBuffer buf) throws IOException {
        this.header.setQuestionEntries((short) this.questions.size());
        this.header.setAnswerEntries((short) this.answers.size());
        this.header.setAuthorityRecords((short) this.authority.size());
        this.header.setAdditionalRecords((short) this.additional.size());
        this.header.toBytes(buf);
        for (Question q : this.questions) {
            q.toBytes(buf);
        }
        for (Record r : this.answers) {
            r.toBytes(buf);
        }
        for (Record r2 : this.authority) {
            r2.toBytes(buf);
        }
        for (Record r3 : this.additional) {
            r3.toBytes(buf);
        }
        return this;
    }

    @Override // com.android.server.connectivity.networkrecovery.dnsresolve.MessageContent
    public Message fromBytes(ByteBuffer buf) throws IOException {
        this.header = new Header();
        this.header.fromBytes(buf);
        this.questions.clear();
        this.answers.clear();
        this.authority.clear();
        this.additional.clear();
        if (!this.header.isAvailable()) {
            this.avivableflag = false;
            return this;
        }
        for (int i = 0; i < this.header.getQuestionEntries(); i++) {
            try {
                this.questions.add(new Question().fromBytes(buf));
            } catch (BufferUnderflowException e) {
                System.err.println("catch  BufferUnderflowException");
            }
        }
        for (int i2 = 0; i2 < this.header.getAnswerEntries(); i2++) {
            this.answers.add(new Record().fromBytes(buf));
        }
        return this;
    }

    public String toString() {
        return "Message [header=" + this.header + ", questions=" + this.questions + ", answers=" + this.answers + ", authority=" + this.authority + ", additional=" + this.additional + "]";
    }
}
