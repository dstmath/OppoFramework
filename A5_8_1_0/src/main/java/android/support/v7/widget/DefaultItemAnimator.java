package android.support.v7.widget;

import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v7.widget.RecyclerView.ItemAnimator;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class DefaultItemAnimator extends ItemAnimator {
    private static final boolean DEBUG = false;
    private ArrayList<ViewHolder> mAddAnimations = new ArrayList();
    private ArrayList<ArrayList<ViewHolder>> mAdditionsList = new ArrayList();
    private ArrayList<ViewHolder> mChangeAnimations = new ArrayList();
    private ArrayList<ArrayList<ChangeInfo>> mChangesList = new ArrayList();
    private ArrayList<ViewHolder> mMoveAnimations = new ArrayList();
    private ArrayList<ArrayList<MoveInfo>> mMovesList = new ArrayList();
    private ArrayList<ViewHolder> mPendingAdditions = new ArrayList();
    private ArrayList<ChangeInfo> mPendingChanges = new ArrayList();
    private ArrayList<MoveInfo> mPendingMoves = new ArrayList();
    private ArrayList<ViewHolder> mPendingRemovals = new ArrayList();
    private ArrayList<ViewHolder> mRemoveAnimations = new ArrayList();

    private static class VpaListenerAdapter implements ViewPropertyAnimatorListener {
        /* synthetic */ VpaListenerAdapter(VpaListenerAdapter -this0) {
            this();
        }

        private VpaListenerAdapter() {
        }

        public void onAnimationStart(View view) {
        }

        public void onAnimationEnd(View view) {
        }

        public void onAnimationCancel(View view) {
        }
    }

    private static class ChangeInfo {
        public int fromX;
        public int fromY;
        public ViewHolder newHolder;
        public ViewHolder oldHolder;
        public int toX;
        public int toY;

        /* synthetic */ ChangeInfo(ViewHolder oldHolder, ViewHolder newHolder, int fromX, int fromY, int toX, int toY, ChangeInfo -this6) {
            this(oldHolder, newHolder, fromX, fromY, toX, toY);
        }

        private ChangeInfo(ViewHolder oldHolder, ViewHolder newHolder) {
            this.oldHolder = oldHolder;
            this.newHolder = newHolder;
        }

        private ChangeInfo(ViewHolder oldHolder, ViewHolder newHolder, int fromX, int fromY, int toX, int toY) {
            this(oldHolder, newHolder);
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX = toX;
            this.toY = toY;
        }

        public String toString() {
            return "ChangeInfo{oldHolder=" + this.oldHolder + ", newHolder=" + this.newHolder + ", fromX=" + this.fromX + ", fromY=" + this.fromY + ", toX=" + this.toX + ", toY=" + this.toY + '}';
        }
    }

    private static class MoveInfo {
        public int fromX;
        public int fromY;
        public ViewHolder holder;
        public int toX;
        public int toY;

        /* synthetic */ MoveInfo(ViewHolder holder, int fromX, int fromY, int toX, int toY, MoveInfo -this5) {
            this(holder, fromX, fromY, toX, toY);
        }

        private MoveInfo(ViewHolder holder, int fromX, int fromY, int toX, int toY) {
            this.holder = holder;
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX = toX;
            this.toY = toY;
        }
    }

    public void runPendingAnimations() {
        boolean removalsPending = this.mPendingRemovals.isEmpty() ^ 1;
        boolean movesPending = this.mPendingMoves.isEmpty() ^ 1;
        boolean changesPending = this.mPendingChanges.isEmpty() ^ 1;
        boolean additionsPending = this.mPendingAdditions.isEmpty() ^ 1;
        if (removalsPending || (movesPending ^ 1) == 0 || (additionsPending ^ 1) == 0 || (changesPending ^ 1) == 0) {
            for (ViewHolder holder : this.mPendingRemovals) {
                animateRemoveImpl(holder);
            }
            this.mPendingRemovals.clear();
            if (movesPending) {
                ArrayList<MoveInfo> moves = new ArrayList();
                moves.addAll(this.mPendingMoves);
                this.mMovesList.add(moves);
                this.mPendingMoves.clear();
                final ArrayList<MoveInfo> arrayList = moves;
                Runnable anonymousClass1 = new Runnable() {
                    public void run() {
                        for (MoveInfo moveInfo : arrayList) {
                            DefaultItemAnimator.this.animateMoveImpl(moveInfo.holder, moveInfo.fromX, moveInfo.fromY, moveInfo.toX, moveInfo.toY);
                        }
                        arrayList.clear();
                        DefaultItemAnimator.this.mMovesList.remove(arrayList);
                    }
                };
                if (removalsPending) {
                    ViewCompat.postOnAnimationDelayed(((MoveInfo) moves.get(0)).holder.itemView, anonymousClass1, getRemoveDuration());
                } else {
                    anonymousClass1.run();
                }
            }
            if (changesPending) {
                final ArrayList<ChangeInfo> changes = new ArrayList();
                changes.addAll(this.mPendingChanges);
                this.mChangesList.add(changes);
                this.mPendingChanges.clear();
                Runnable changer = new Runnable() {
                    public void run() {
                        for (ChangeInfo change : changes) {
                            DefaultItemAnimator.this.animateChangeImpl(change);
                        }
                        changes.clear();
                        DefaultItemAnimator.this.mChangesList.remove(changes);
                    }
                };
                if (removalsPending) {
                    ViewCompat.postOnAnimationDelayed(((ChangeInfo) changes.get(0)).oldHolder.itemView, changer, getRemoveDuration());
                } else {
                    changer.run();
                }
            }
            if (additionsPending) {
                final ArrayList<ViewHolder> additions = new ArrayList();
                additions.addAll(this.mPendingAdditions);
                this.mAdditionsList.add(additions);
                this.mPendingAdditions.clear();
                Runnable adder = new Runnable() {
                    public void run() {
                        for (ViewHolder holder : additions) {
                            DefaultItemAnimator.this.animateAddImpl(holder);
                        }
                        additions.clear();
                        DefaultItemAnimator.this.mAdditionsList.remove(additions);
                    }
                };
                if (removalsPending || movesPending || changesPending) {
                    ViewCompat.postOnAnimationDelayed(((ViewHolder) additions.get(0)).itemView, adder, (removalsPending ? getRemoveDuration() : 0) + Math.max(movesPending ? getMoveDuration() : 0, changesPending ? getChangeDuration() : 0));
                } else {
                    adder.run();
                }
            }
        }
    }

    public boolean animateRemove(ViewHolder holder) {
        endAnimation(holder);
        this.mPendingRemovals.add(holder);
        return true;
    }

    private void animateRemoveImpl(final ViewHolder holder) {
        final ViewPropertyAnimatorCompat animation = ViewCompat.animate(holder.itemView);
        animation.setDuration(getRemoveDuration()).alpha(0.0f).setListener(new VpaListenerAdapter() {
            public void onAnimationStart(View view) {
                DefaultItemAnimator.this.dispatchRemoveStarting(holder);
            }

            public void onAnimationEnd(View view) {
                animation.setListener(null);
                ViewCompat.setAlpha(view, 1.0f);
                DefaultItemAnimator.this.dispatchRemoveFinished(holder);
                DefaultItemAnimator.this.mRemoveAnimations.remove(holder);
                DefaultItemAnimator.this.dispatchFinishedWhenDone();
            }
        }).start();
        this.mRemoveAnimations.add(holder);
    }

    public boolean animateAdd(ViewHolder holder) {
        endAnimation(holder);
        ViewCompat.setAlpha(holder.itemView, 0.0f);
        this.mPendingAdditions.add(holder);
        return true;
    }

    private void animateAddImpl(final ViewHolder holder) {
        View view = holder.itemView;
        this.mAddAnimations.add(holder);
        final ViewPropertyAnimatorCompat animation = ViewCompat.animate(view);
        animation.alpha(1.0f).setDuration(getAddDuration()).setListener(new VpaListenerAdapter() {
            public void onAnimationStart(View view) {
                DefaultItemAnimator.this.dispatchAddStarting(holder);
            }

            public void onAnimationCancel(View view) {
                ViewCompat.setAlpha(view, 1.0f);
            }

            public void onAnimationEnd(View view) {
                animation.setListener(null);
                DefaultItemAnimator.this.dispatchAddFinished(holder);
                DefaultItemAnimator.this.mAddAnimations.remove(holder);
                DefaultItemAnimator.this.dispatchFinishedWhenDone();
            }
        }).start();
    }

    public boolean animateMove(ViewHolder holder, int fromX, int fromY, int toX, int toY) {
        View view = holder.itemView;
        fromX = (int) (((float) fromX) + ViewCompat.getTranslationX(holder.itemView));
        fromY = (int) (((float) fromY) + ViewCompat.getTranslationY(holder.itemView));
        endAnimation(holder);
        int deltaX = toX - fromX;
        int deltaY = toY - fromY;
        if (deltaX == 0 && deltaY == 0) {
            dispatchMoveFinished(holder);
            return false;
        }
        if (deltaX != 0) {
            ViewCompat.setTranslationX(view, (float) (-deltaX));
        }
        if (deltaY != 0) {
            ViewCompat.setTranslationY(view, (float) (-deltaY));
        }
        this.mPendingMoves.add(new MoveInfo(holder, fromX, fromY, toX, toY, null));
        return true;
    }

    private void animateMoveImpl(ViewHolder holder, int fromX, int fromY, int toX, int toY) {
        View view = holder.itemView;
        final int deltaX = toX - fromX;
        final int deltaY = toY - fromY;
        if (deltaX != 0) {
            ViewCompat.animate(view).translationX(0.0f);
        }
        if (deltaY != 0) {
            ViewCompat.animate(view).translationY(0.0f);
        }
        this.mMoveAnimations.add(holder);
        final ViewPropertyAnimatorCompat animation = ViewCompat.animate(view);
        final ViewHolder viewHolder = holder;
        animation.setDuration(getMoveDuration()).setListener(new VpaListenerAdapter() {
            public void onAnimationStart(View view) {
                DefaultItemAnimator.this.dispatchMoveStarting(viewHolder);
            }

            public void onAnimationCancel(View view) {
                if (deltaX != 0) {
                    ViewCompat.setTranslationX(view, 0.0f);
                }
                if (deltaY != 0) {
                    ViewCompat.setTranslationY(view, 0.0f);
                }
            }

            public void onAnimationEnd(View view) {
                animation.setListener(null);
                DefaultItemAnimator.this.dispatchMoveFinished(viewHolder);
                DefaultItemAnimator.this.mMoveAnimations.remove(viewHolder);
                DefaultItemAnimator.this.dispatchFinishedWhenDone();
            }
        }).start();
    }

    public boolean animateChange(ViewHolder oldHolder, ViewHolder newHolder, int fromX, int fromY, int toX, int toY) {
        float prevTranslationX = ViewCompat.getTranslationX(oldHolder.itemView);
        float prevTranslationY = ViewCompat.getTranslationY(oldHolder.itemView);
        float prevAlpha = ViewCompat.getAlpha(oldHolder.itemView);
        endAnimation(oldHolder);
        int deltaX = (int) (((float) (toX - fromX)) - prevTranslationX);
        int deltaY = (int) (((float) (toY - fromY)) - prevTranslationY);
        ViewCompat.setTranslationX(oldHolder.itemView, prevTranslationX);
        ViewCompat.setTranslationY(oldHolder.itemView, prevTranslationY);
        ViewCompat.setAlpha(oldHolder.itemView, prevAlpha);
        if (!(newHolder == null || newHolder.itemView == null)) {
            endAnimation(newHolder);
            ViewCompat.setTranslationX(newHolder.itemView, (float) (-deltaX));
            ViewCompat.setTranslationY(newHolder.itemView, (float) (-deltaY));
            ViewCompat.setAlpha(newHolder.itemView, 0.0f);
        }
        this.mPendingChanges.add(new ChangeInfo(oldHolder, newHolder, fromX, fromY, toX, toY, null));
        return true;
    }

    private void animateChangeImpl(final ChangeInfo changeInfo) {
        View view = changeInfo.oldHolder.itemView;
        ViewHolder newHolder = changeInfo.newHolder;
        final View newView = newHolder != null ? newHolder.itemView : null;
        this.mChangeAnimations.add(changeInfo.oldHolder);
        final ViewPropertyAnimatorCompat oldViewAnim = ViewCompat.animate(view).setDuration(getChangeDuration());
        oldViewAnim.translationX((float) (changeInfo.toX - changeInfo.fromX));
        oldViewAnim.translationY((float) (changeInfo.toY - changeInfo.fromY));
        oldViewAnim.alpha(0.0f).setListener(new VpaListenerAdapter() {
            public void onAnimationStart(View view) {
                DefaultItemAnimator.this.dispatchChangeStarting(changeInfo.oldHolder, true);
            }

            public void onAnimationEnd(View view) {
                oldViewAnim.setListener(null);
                ViewCompat.setAlpha(view, 1.0f);
                ViewCompat.setTranslationX(view, 0.0f);
                ViewCompat.setTranslationY(view, 0.0f);
                DefaultItemAnimator.this.dispatchChangeFinished(changeInfo.oldHolder, true);
                DefaultItemAnimator.this.mChangeAnimations.remove(changeInfo.oldHolder);
                DefaultItemAnimator.this.dispatchFinishedWhenDone();
            }
        }).start();
        if (newView != null) {
            this.mChangeAnimations.add(changeInfo.newHolder);
            final ViewPropertyAnimatorCompat newViewAnimation = ViewCompat.animate(newView);
            newViewAnimation.translationX(0.0f).translationY(0.0f).setDuration(getChangeDuration()).alpha(1.0f).setListener(new VpaListenerAdapter() {
                public void onAnimationStart(View view) {
                    DefaultItemAnimator.this.dispatchChangeStarting(changeInfo.newHolder, false);
                }

                public void onAnimationEnd(View view) {
                    newViewAnimation.setListener(null);
                    ViewCompat.setAlpha(newView, 1.0f);
                    ViewCompat.setTranslationX(newView, 0.0f);
                    ViewCompat.setTranslationY(newView, 0.0f);
                    DefaultItemAnimator.this.dispatchChangeFinished(changeInfo.newHolder, false);
                    DefaultItemAnimator.this.mChangeAnimations.remove(changeInfo.newHolder);
                    DefaultItemAnimator.this.dispatchFinishedWhenDone();
                }
            }).start();
        }
    }

    private void endChangeAnimation(List<ChangeInfo> infoList, ViewHolder item) {
        for (int i = infoList.size() - 1; i >= 0; i--) {
            ChangeInfo changeInfo = (ChangeInfo) infoList.get(i);
            if (endChangeAnimationIfNecessary(changeInfo, item) && changeInfo.oldHolder == null && changeInfo.newHolder == null) {
                infoList.remove(changeInfo);
            }
        }
    }

    private void endChangeAnimationIfNecessary(ChangeInfo changeInfo) {
        if (changeInfo.oldHolder != null) {
            endChangeAnimationIfNecessary(changeInfo, changeInfo.oldHolder);
        }
        if (changeInfo.newHolder != null) {
            endChangeAnimationIfNecessary(changeInfo, changeInfo.newHolder);
        }
    }

    private boolean endChangeAnimationIfNecessary(ChangeInfo changeInfo, ViewHolder item) {
        boolean oldItem = false;
        if (changeInfo.newHolder == item) {
            changeInfo.newHolder = null;
        } else if (changeInfo.oldHolder != item) {
            return false;
        } else {
            changeInfo.oldHolder = null;
            oldItem = true;
        }
        ViewCompat.setAlpha(item.itemView, 1.0f);
        ViewCompat.setTranslationX(item.itemView, 0.0f);
        ViewCompat.setTranslationY(item.itemView, 0.0f);
        dispatchChangeFinished(item, oldItem);
        return true;
    }

    public void endAnimation(ViewHolder item) {
        int i;
        View view = item.itemView;
        ViewCompat.animate(view).cancel();
        for (i = this.mPendingMoves.size() - 1; i >= 0; i--) {
            if (((MoveInfo) this.mPendingMoves.get(i)).holder == item) {
                ViewCompat.setTranslationY(view, 0.0f);
                ViewCompat.setTranslationX(view, 0.0f);
                dispatchMoveFinished(item);
                this.mPendingMoves.remove(item);
            }
        }
        endChangeAnimation(this.mPendingChanges, item);
        if (this.mPendingRemovals.remove(item)) {
            ViewCompat.setAlpha(view, 1.0f);
            dispatchRemoveFinished(item);
        }
        if (this.mPendingAdditions.remove(item)) {
            ViewCompat.setAlpha(view, 1.0f);
            dispatchAddFinished(item);
        }
        for (i = this.mChangesList.size() - 1; i >= 0; i--) {
            ArrayList<ChangeInfo> changes = (ArrayList) this.mChangesList.get(i);
            endChangeAnimation(changes, item);
            if (changes.isEmpty()) {
                this.mChangesList.remove(changes);
            }
        }
        for (i = this.mMovesList.size() - 1; i >= 0; i--) {
            ArrayList<MoveInfo> moves = (ArrayList) this.mMovesList.get(i);
            int j = moves.size() - 1;
            while (j >= 0) {
                if (((MoveInfo) moves.get(j)).holder == item) {
                    ViewCompat.setTranslationY(view, 0.0f);
                    ViewCompat.setTranslationX(view, 0.0f);
                    dispatchMoveFinished(item);
                    moves.remove(j);
                    if (moves.isEmpty()) {
                        this.mMovesList.remove(moves);
                    }
                } else {
                    j--;
                }
            }
        }
        for (i = this.mAdditionsList.size() - 1; i >= 0; i--) {
            ArrayList<ViewHolder> additions = (ArrayList) this.mAdditionsList.get(i);
            if (additions.remove(item)) {
                ViewCompat.setAlpha(view, 1.0f);
                dispatchAddFinished(item);
                if (additions.isEmpty()) {
                    this.mAdditionsList.remove(additions);
                }
            }
        }
        boolean remove = this.mRemoveAnimations.remove(item);
        remove = this.mAddAnimations.remove(item);
        remove = this.mChangeAnimations.remove(item);
        remove = this.mMoveAnimations.remove(item);
        dispatchFinishedWhenDone();
    }

    public boolean isRunning() {
        return (this.mPendingAdditions.isEmpty() && (this.mPendingChanges.isEmpty() ^ 1) == 0 && (this.mPendingMoves.isEmpty() ^ 1) == 0 && (this.mPendingRemovals.isEmpty() ^ 1) == 0 && (this.mMoveAnimations.isEmpty() ^ 1) == 0 && (this.mRemoveAnimations.isEmpty() ^ 1) == 0 && (this.mAddAnimations.isEmpty() ^ 1) == 0 && (this.mChangeAnimations.isEmpty() ^ 1) == 0 && (this.mMovesList.isEmpty() ^ 1) == 0 && (this.mAdditionsList.isEmpty() ^ 1) == 0) ? this.mChangesList.isEmpty() ^ 1 : true;
    }

    private void dispatchFinishedWhenDone() {
        if (!isRunning()) {
            dispatchAnimationsFinished();
        }
    }

    public void endAnimations() {
        int i;
        View view;
        ViewHolder item;
        for (i = this.mPendingMoves.size() - 1; i >= 0; i--) {
            MoveInfo item2 = (MoveInfo) this.mPendingMoves.get(i);
            view = item2.holder.itemView;
            ViewCompat.setTranslationY(view, 0.0f);
            ViewCompat.setTranslationX(view, 0.0f);
            dispatchMoveFinished(item2.holder);
            this.mPendingMoves.remove(i);
        }
        for (i = this.mPendingRemovals.size() - 1; i >= 0; i--) {
            dispatchRemoveFinished((ViewHolder) this.mPendingRemovals.get(i));
            this.mPendingRemovals.remove(i);
        }
        for (i = this.mPendingAdditions.size() - 1; i >= 0; i--) {
            item = (ViewHolder) this.mPendingAdditions.get(i);
            ViewCompat.setAlpha(item.itemView, 1.0f);
            dispatchAddFinished(item);
            this.mPendingAdditions.remove(i);
        }
        for (i = this.mPendingChanges.size() - 1; i >= 0; i--) {
            endChangeAnimationIfNecessary((ChangeInfo) this.mPendingChanges.get(i));
        }
        this.mPendingChanges.clear();
        if (isRunning()) {
            int j;
            for (i = this.mMovesList.size() - 1; i >= 0; i--) {
                ArrayList<MoveInfo> moves = (ArrayList) this.mMovesList.get(i);
                for (j = moves.size() - 1; j >= 0; j--) {
                    MoveInfo moveInfo = (MoveInfo) moves.get(j);
                    view = moveInfo.holder.itemView;
                    ViewCompat.setTranslationY(view, 0.0f);
                    ViewCompat.setTranslationX(view, 0.0f);
                    dispatchMoveFinished(moveInfo.holder);
                    moves.remove(j);
                    if (moves.isEmpty()) {
                        this.mMovesList.remove(moves);
                    }
                }
            }
            for (i = this.mAdditionsList.size() - 1; i >= 0; i--) {
                ArrayList<ViewHolder> additions = (ArrayList) this.mAdditionsList.get(i);
                for (j = additions.size() - 1; j >= 0; j--) {
                    item = (ViewHolder) additions.get(j);
                    ViewCompat.setAlpha(item.itemView, 1.0f);
                    dispatchAddFinished(item);
                    additions.remove(j);
                    if (additions.isEmpty()) {
                        this.mAdditionsList.remove(additions);
                    }
                }
            }
            for (i = this.mChangesList.size() - 1; i >= 0; i--) {
                ArrayList<ChangeInfo> changes = (ArrayList) this.mChangesList.get(i);
                for (j = changes.size() - 1; j >= 0; j--) {
                    endChangeAnimationIfNecessary((ChangeInfo) changes.get(j));
                    if (changes.isEmpty()) {
                        this.mChangesList.remove(changes);
                    }
                }
            }
            cancelAll(this.mRemoveAnimations);
            cancelAll(this.mMoveAnimations);
            cancelAll(this.mAddAnimations);
            cancelAll(this.mChangeAnimations);
            dispatchAnimationsFinished();
        }
    }

    void cancelAll(List<ViewHolder> viewHolders) {
        for (int i = viewHolders.size() - 1; i >= 0; i--) {
            ViewCompat.animate(((ViewHolder) viewHolders.get(i)).itemView).cancel();
        }
    }
}
