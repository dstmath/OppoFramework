package com.android.internal.app;

import android.animation.Animator;

/* compiled from: OppoWindowDecorActionBar */
interface SearchAnimatorListener {
    void onSearchAnimationCancel(Animator animator, boolean z);

    void onSearchAnimationEnd(Animator animator, boolean z);

    void onSearchAnimationRepeat(Animator animator, boolean z);

    void onSearchAnimationStart(Animator animator, boolean z);
}
