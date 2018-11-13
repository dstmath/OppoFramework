package com.color.widget.help;

import android.animation.Animator;
import com.color.widget.ColorDeleteAnimation;
import com.color.widget.ColorRecyclerView;
import com.color.widget.ColorRecyclerView.ItemAnimator;
import com.color.widget.ColorRecyclerView.ViewHolder;
import java.util.ArrayList;
import java.util.List;

public class ColorItemDeleteHelper {
    List<ColorDeleteAnimation> mRecoverAnimations = new ArrayList();
    public ColorRecyclerView mRecyclerView;

    public ColorItemDeleteHelper(ColorRecyclerView view) {
        this.mRecyclerView = view;
    }

    public void startDeleteAnimation(ViewHolder viewHolder, float startDx, float startDy, float targetX, float targetY) {
        final ViewHolder viewHolder2 = viewHolder;
        ColorDeleteAnimation rv = new ColorDeleteAnimation(viewHolder, 0.0f, 0.0f, (float) this.mRecyclerView.getWidth(), 0.0f) {
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (!this.mOverridden) {
                    ColorItemDeleteHelper.this.postDispatchSwipe(this, 8, viewHolder2);
                }
            }
        };
        rv.setDuration(200);
        this.mRecoverAnimations.add(rv);
        rv.start();
    }

    private void postDispatchSwipe(final ColorDeleteAnimation anim, int swipeDir, final ViewHolder holder) {
        this.mRecyclerView.post(new Runnable() {
            public void run() {
                if (ColorItemDeleteHelper.this.mRecyclerView != null && ColorItemDeleteHelper.this.mRecyclerView.isAttachedToWindow() && (anim.mOverridden ^ 1) != 0 && anim.mViewHolder.getAdapterPosition() != -1) {
                    ItemAnimator animator = ColorItemDeleteHelper.this.mRecyclerView.getItemAnimator();
                    if ((animator == null || (animator.isRunning(null) ^ 1) != 0) && (ColorItemDeleteHelper.this.hasRunningRecoverAnim() ^ 1) != 0) {
                        ColorItemDeleteHelper.this.mRecyclerView.getAdapter().delete(holder.getAdapterPosition());
                    } else {
                        ColorItemDeleteHelper.this.mRecyclerView.post(this);
                    }
                }
            }
        });
    }

    private boolean hasRunningRecoverAnim() {
        int size = this.mRecoverAnimations.size();
        for (int i = 0; i < size; i++) {
            if (!((ColorDeleteAnimation) this.mRecoverAnimations.get(i)).mEnded) {
                return true;
            }
        }
        return false;
    }
}
