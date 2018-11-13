package com.android.internal.widget;

import android.animation.Animator;

/* compiled from: ColorActionBarContextView */
interface UserAnimatorListener {
    void onAnimationCancel(Animator animator, boolean z);

    void onAnimationEnd(Animator animator, boolean z);

    void onAnimationRepeat(Animator animator, boolean z);

    void onAnimationStart(Animator animator, boolean z);
}
