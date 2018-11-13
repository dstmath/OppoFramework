package com.android.server.accounts;

import android.accounts.Account;

final /* synthetic */ class AccountManagerService$-boolean_removeAccountInternal_com_android_server_accounts_AccountManagerService$UserAccounts_accounts_android_accounts_Account_account_int_callingUid_LambdaImpl0 implements Runnable {
    private /* synthetic */ Account val$account;
    private /* synthetic */ AccountManagerService val$this;
    private /* synthetic */ int val$uid;

    public /* synthetic */ AccountManagerService$-boolean_removeAccountInternal_com_android_server_accounts_AccountManagerService$UserAccounts_accounts_android_accounts_Account_account_int_callingUid_LambdaImpl0(AccountManagerService accountManagerService, Account account, int i) {
        this.val$this = accountManagerService;
        this.val$account = account;
        this.val$uid = i;
    }

    public void run() {
        this.val$this.m14-com_android_server_accounts_AccountManagerService_lambda$2(this.val$account, this.val$uid);
    }
}
