package com.android.server.wm;

public final class OppoTransactionFactory {
    TransactionFactory mTransactinFactory;

    public OppoTransactionFactory(TransactionFactory transactionFactory) {
        this.mTransactinFactory = transactionFactory;
    }
}
