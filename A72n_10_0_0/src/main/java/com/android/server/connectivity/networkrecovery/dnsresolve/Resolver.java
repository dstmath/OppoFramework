package com.android.server.connectivity.networkrecovery.dnsresolve;

import com.android.server.connectivity.networkrecovery.dnsresolve.Header;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Random;

public class Resolver {
    public static final int DNS_PORT = 53;
    private static final Random RANDOM = new Random();
    private final InetAddress dnsServer;
    private int mQuerryTimeout = 2000;
    private int mRetryCount = 2;
    private boolean useUdp = true;

    public Resolver(InetAddress dnsServer2, int querryTimeout, int retryCount) {
        this.dnsServer = dnsServer2;
        this.mQuerryTimeout = querryTimeout;
        this.mRetryCount = retryCount;
    }

    public void setUseUdp(boolean useUdp2) {
        this.useUdp = useUdp2;
    }

    public Message request(String domain) throws IOException {
        return request(domain, Type.ALL);
    }

    public Message request(String domain, Type questionType) throws IOException {
        return request(domain, RecordClass.IN, questionType);
    }

    public Message request(String domain, RecordClass questionClass) throws IOException {
        return request(domain, questionClass, Type.ALL);
    }

    public Message request(String domain, RecordClass questionClass, Type questionType) throws IOException {
        return getReply(createMessage(domain, questionClass, questionType));
    }

    public Message getReply(Message requestMessage) throws IOException {
        if (requestMessage.getHeader().getId() == 0) {
            requestMessage.getHeader().setId((short) RANDOM.nextInt(32768));
        }
        return getReply(requestMessage, new UdpTransport(this.dnsServer, this.mQuerryTimeout, this.mRetryCount));
    }

    public Message getReply(Message requestMessage, DnsTransport transport) throws IOException {
        return transport.sendQuery(requestMessage);
    }

    private Message createMessage(String domain, RecordClass questionClass, Type questionType) {
        Question question = new Question();
        question.setDomain(Domain.fromQName(domain));
        question.setQuestionClass(questionClass);
        question.setQuestionType(questionType);
        Header header = new Header();
        header.setOpcode(Header.Opcode.QUERY);
        header.setRequest(true);
        header.setRecursionDesired(true);
        Message requestMessage = new Message();
        requestMessage.setHeader(header);
        requestMessage.getQuestions().add(question);
        return requestMessage;
    }
}
