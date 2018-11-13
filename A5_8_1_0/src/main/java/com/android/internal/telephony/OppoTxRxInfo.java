package com.android.internal.telephony;

public class OppoTxRxInfo {
    private OppoRxChainInfo mRxChain0;
    private int mRxChain0Valid;
    private OppoRxChainInfo mRxChain1;
    private int mRxChain1Valid;
    private OppoRxChainInfo mRxChain2;
    private int mRxChain2Valid;
    private OppoRxChainInfo mRxChain3;
    private int mRxChain3Valid;
    private OppoTxInfo mTx;
    private int mTxValid;

    public OppoTxRxInfo(int mRxChain0Valid, OppoRxChainInfo mRxChain0, int mRxChain1Valid, OppoRxChainInfo mRxChain1, int mRxChain2Valid, OppoRxChainInfo mRxChain2, int mRxChain3Valid, OppoRxChainInfo mRxChain3, int mTxValid, OppoTxInfo mTx) {
        this.mRxChain0Valid = mRxChain0Valid;
        this.mRxChain0 = mRxChain0;
        this.mRxChain1Valid = mRxChain1Valid;
        this.mRxChain1 = mRxChain1;
        this.mRxChain2Valid = mRxChain2Valid;
        this.mRxChain2 = mRxChain2;
        this.mRxChain3Valid = mRxChain3Valid;
        this.mRxChain3 = mRxChain3;
        this.mTxValid = mTxValid;
        this.mTx = mTx;
    }

    public int getRxChain0Valid() {
        return this.mRxChain0Valid;
    }

    public int getRxChain1Valid() {
        return this.mRxChain1Valid;
    }

    public int getRxChain2Valid() {
        return this.mRxChain2Valid;
    }

    public int getRxChain3Valid() {
        return this.mRxChain3Valid;
    }

    public int getTxValid() {
        return this.mTxValid;
    }

    public OppoRxChainInfo getRxChain0() {
        return this.mRxChain0;
    }

    public OppoRxChainInfo getRxChain1() {
        return this.mRxChain1;
    }

    public OppoRxChainInfo getRxChain2() {
        return this.mRxChain2;
    }

    public OppoRxChainInfo getRxChain3() {
        return this.mRxChain3;
    }

    public OppoTxInfo getTx() {
        return this.mTx;
    }

    public String toString() {
        return "mRxChain0Valid=" + this.mRxChain0Valid + ", mRxChain0=(" + this.mRxChain0.toString() + "), mRxChain1Valid=" + this.mRxChain1Valid + ", mRxChain1=(" + this.mRxChain1.toString() + ")," + this.mRxChain2Valid + ", mRxChain2=(" + this.mRxChain2.toString() + "), mRxChain3Valid=" + this.mRxChain3Valid + ", mRxChain3=(" + this.mRxChain3.toString() + ")," + "mTxValid=" + this.mTxValid + ", mTx=(" + this.mTx.toString() + ")";
    }
}
