package com.color.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.internal.widget.RecyclerView;
import com.color.util.ColorChangeTextUtil;
import java.util.List;

public class ColorResolverGridAdapter extends RecyclerView.Adapter<GridViewHolder> {
    private List<ColorItem> colorItems;
    private int mAppNameSize;
    private int mMoreIconIndex = -1;
    private OnItemClickListener mOnItemClickListener;
    private int mPagerNumber;

    public interface OnItemClickListener {
        void onItemClick(int i, int i2);

        void onItemLongClick(int i, int i2);
    }

    public ColorResolverGridAdapter(Context context) {
        this.mAppNameSize = (int) ColorChangeTextUtil.getSuitableFontSize((float) context.getResources().getDimensionPixelSize(201655745), context.getResources().getConfiguration().fontScale, 2);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    @Override // com.android.internal.widget.RecyclerView.Adapter
    public GridViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new GridViewHolder(LayoutInflater.from(parent.getContext()).inflate(201917622, parent, false));
    }

    public void onBindViewHolder(GridViewHolder holder, int position) {
        List<ColorItem> list = this.colorItems;
        if (list != null && list.size() > position) {
            holder.mIcon.setImageDrawable(this.colorItems.get(position).getIcon());
            holder.mIcon.setContentDescription(this.colorItems.get(position).getText());
            holder.mName.setText(this.colorItems.get(position).getText());
            holder.mName.setTextSize(0, (float) this.mAppNameSize);
            holder.itemView.setOnClickListener(new View.OnClickListener(position) {
                /* class com.color.widget.$$Lambda$ColorResolverGridAdapter$rOf7FNNC6FqT5aW73qGf0xyabY */
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    ColorResolverGridAdapter.this.lambda$onBindViewHolder$0$ColorResolverGridAdapter(this.f$1, view);
                }
            });
            holder.mIcon.setOnClickListener(new View.OnClickListener(position) {
                /* class com.color.widget.$$Lambda$ColorResolverGridAdapter$kJVViYrnHNmD8jE6tNQ060q0ehI */
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    ColorResolverGridAdapter.this.lambda$onBindViewHolder$1$ColorResolverGridAdapter(this.f$1, view);
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener(position) {
                /* class com.color.widget.$$Lambda$ColorResolverGridAdapter$Yswra5jEyBXTOu5WteNI0aevxu8 */
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                @Override // android.view.View.OnLongClickListener
                public final boolean onLongClick(View view) {
                    return ColorResolverGridAdapter.this.lambda$onBindViewHolder$2$ColorResolverGridAdapter(this.f$1, view);
                }
            });
            holder.mIcon.setOnLongClickListener(new View.OnLongClickListener(position) {
                /* class com.color.widget.$$Lambda$ColorResolverGridAdapter$ySG9655cxcfrJPDnISNG2UX3mXg */
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                @Override // android.view.View.OnLongClickListener
                public final boolean onLongClick(View view) {
                    return ColorResolverGridAdapter.this.lambda$onBindViewHolder$3$ColorResolverGridAdapter(this.f$1, view);
                }
            });
        }
    }

    public /* synthetic */ void lambda$onBindViewHolder$0$ColorResolverGridAdapter(int position, View view) {
        OnItemClickListener onItemClickListener = this.mOnItemClickListener;
        if (onItemClickListener != null) {
            onItemClickListener.onItemClick(this.mPagerNumber, position);
        }
    }

    public /* synthetic */ void lambda$onBindViewHolder$1$ColorResolverGridAdapter(int position, View view) {
        OnItemClickListener onItemClickListener = this.mOnItemClickListener;
        if (onItemClickListener != null) {
            onItemClickListener.onItemClick(this.mPagerNumber, position);
        }
    }

    public /* synthetic */ boolean lambda$onBindViewHolder$2$ColorResolverGridAdapter(int position, View view) {
        OnItemClickListener onItemClickListener = this.mOnItemClickListener;
        if (onItemClickListener == null) {
            return true;
        }
        onItemClickListener.onItemLongClick(this.mPagerNumber, position);
        return true;
    }

    public /* synthetic */ boolean lambda$onBindViewHolder$3$ColorResolverGridAdapter(int position, View view) {
        OnItemClickListener onItemClickListener = this.mOnItemClickListener;
        if (onItemClickListener == null) {
            return true;
        }
        onItemClickListener.onItemLongClick(this.mPagerNumber, position);
        return true;
    }

    @Override // com.android.internal.widget.RecyclerView.Adapter
    public int getItemCount() {
        List<ColorItem> list = this.colorItems;
        if (list == null) {
            return 0;
        }
        int i = this.mMoreIconIndex;
        return i != -1 ? i + 1 : list.size();
    }

    public void setColorItems(List<ColorItem> colorItems2) {
        this.colorItems = colorItems2;
    }

    public void setPagerNumber(int pagerNumber) {
        this.mPagerNumber = pagerNumber;
    }

    public void startMoreAnimation(int iconIndex) {
        this.mMoreIconIndex = iconIndex;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            /* class com.color.widget.$$Lambda$ColorResolverGridAdapter$kDlrxiHshcWc2Jm7c0pzc4oN3NU */

            public final void run() {
                ColorResolverGridAdapter.this.lambda$startMoreAnimation$4$ColorResolverGridAdapter();
            }
        });
    }

    public /* synthetic */ void lambda$startMoreAnimation$4$ColorResolverGridAdapter() {
        this.mMoreIconIndex = -1;
        notifyItemRangeInserted(this.mMoreIconIndex + 1, getItemCount());
    }

    /* access modifiers changed from: package-private */
    public static class GridViewHolder extends RecyclerView.ViewHolder {
        ImageView mIcon;
        TextView mName;

        public GridViewHolder(View itemView) {
            super(itemView);
            this.mIcon = (ImageView) itemView.findViewById(201459066);
            this.mName = (TextView) itemView.findViewById(201459067);
        }
    }
}
