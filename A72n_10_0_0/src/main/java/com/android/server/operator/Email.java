package com.android.server.operator;

public class Email extends Element {
    private String domain;
    private String id;
    private String incomingUriTemplate;
    private String incomingUsernameTemplate;
    private String label;
    private String outgoingUriTemplate;
    private String outgoingUsernameTemplate;
    private String signature;
    private String spn;

    public Email() {
        super("email");
    }

    public String toString() {
        return "Email{id='" + this.id + "', label='" + this.label + "', domain='" + this.domain + "', signature='" + this.signature + "', incomingUriTemplate='" + this.incomingUriTemplate + "', incomingUsernameTemplate='" + this.incomingUsernameTemplate + "', outgoingUriTemplate='" + this.outgoingUriTemplate + "', outgoingUsernameTemplate='" + this.outgoingUsernameTemplate + "', country='" + this.country + "', spn='" + this.spn + "'}";
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id2) {
        this.id = id2;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label2) {
        this.label = label2;
    }

    public String getDomain() {
        return this.domain;
    }

    public void setDomain(String domain2) {
        this.domain = domain2;
    }

    public String getSignature() {
        return this.signature;
    }

    public void setSignature(String signature2) {
        this.signature = signature2;
    }

    public String getIncomingUriTemplate() {
        return this.incomingUriTemplate;
    }

    public void setIncomingUriTemplate(String incomingUriTemplate2) {
        this.incomingUriTemplate = incomingUriTemplate2;
    }

    public String getIncomingUsernameTemplate() {
        return this.incomingUsernameTemplate;
    }

    public void setIncomingUsernameTemplate(String incomingUsernameTemplate2) {
        this.incomingUsernameTemplate = incomingUsernameTemplate2;
    }

    public String getOutgoingUriTemplate() {
        return this.outgoingUriTemplate;
    }

    public void setOutgoingUriTemplate(String outgoingUriTemplate2) {
        this.outgoingUriTemplate = outgoingUriTemplate2;
    }

    public String getOutgoingUsernameTemplate() {
        return this.outgoingUsernameTemplate;
    }

    public void setOutgoingUsernameTemplate(String outgoingUsernameTemplate2) {
        this.outgoingUsernameTemplate = outgoingUsernameTemplate2;
    }

    public String getSpn() {
        return this.spn;
    }

    public void setSpn(String spn2) {
        this.spn = spn2;
    }
}
