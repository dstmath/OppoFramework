package com.android.server.operator;

public class Signature extends Element {
    private String global_signature;
    private String spn;

    public Signature() {
        super("signature");
    }

    public String getGlobal_signature() {
        return this.global_signature;
    }

    public void setGlobal_signature(String global_signature2) {
        this.global_signature = global_signature2;
    }

    public String getSpn() {
        return this.spn;
    }

    public void setSpn(String spn2) {
        this.spn = spn2;
    }

    public String toString() {
        return "Signature{global_signature='" + this.global_signature + '\'' + ", country='" + this.country + '\'' + ", spn='" + this.spn + '\'' + '}';
    }
}
