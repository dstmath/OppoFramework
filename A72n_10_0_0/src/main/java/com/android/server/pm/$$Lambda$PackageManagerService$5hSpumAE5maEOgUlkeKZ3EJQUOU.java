package com.android.server.pm;

import java.util.function.Predicate;

/* renamed from: com.android.server.pm.-$$Lambda$PackageManagerService$5hSpumAE5maEOgUlkeKZ3EJQUOU  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PackageManagerService$5hSpumAE5maEOgUlkeKZ3EJQUOU implements Predicate {
    public static final /* synthetic */ $$Lambda$PackageManagerService$5hSpumAE5maEOgUlkeKZ3EJQUOU INSTANCE = new $$Lambda$PackageManagerService$5hSpumAE5maEOgUlkeKZ3EJQUOU();

    private /* synthetic */ $$Lambda$PackageManagerService$5hSpumAE5maEOgUlkeKZ3EJQUOU() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return PackageManagerService.lambda$unsuspendForNonSystemSuspendingPackages$12((String) obj);
    }
}
