package android.widget;

import android.annotation.UnsupportedAppUsage;
import android.database.DataSetObserver;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.Iterator;

public class HeaderViewListAdapter implements WrapperListAdapter, Filterable {
    static final ArrayList<ListView.FixedViewInfo> EMPTY_INFO_LIST = new ArrayList<>();
    private static final String TAG = "HeaderViewListAdapter";
    @UnsupportedAppUsage
    private final ListAdapter mAdapter;
    boolean mAreAllFixedViewsSelectable;
    @UnsupportedAppUsage
    ArrayList<ListView.FixedViewInfo> mFooterViewInfos;
    @UnsupportedAppUsage
    ArrayList<ListView.FixedViewInfo> mHeaderViewInfos;
    private final boolean mIsFilterable;

    public HeaderViewListAdapter(ArrayList<ListView.FixedViewInfo> headerViewInfos, ArrayList<ListView.FixedViewInfo> footerViewInfos, ListAdapter adapter) {
        this.mAdapter = adapter;
        this.mIsFilterable = adapter instanceof Filterable;
        if (headerViewInfos == null) {
            this.mHeaderViewInfos = EMPTY_INFO_LIST;
        } else {
            this.mHeaderViewInfos = headerViewInfos;
        }
        if (footerViewInfos == null) {
            this.mFooterViewInfos = EMPTY_INFO_LIST;
        } else {
            this.mFooterViewInfos = footerViewInfos;
        }
        this.mAreAllFixedViewsSelectable = areAllListInfosSelectable(this.mHeaderViewInfos) && areAllListInfosSelectable(this.mFooterViewInfos);
    }

    public int getHeadersCount() {
        return this.mHeaderViewInfos.size();
    }

    public int getFootersCount() {
        return this.mFooterViewInfos.size();
    }

    @Override // android.widget.Adapter
    public boolean isEmpty() {
        ListAdapter listAdapter = this.mAdapter;
        return listAdapter == null || listAdapter.isEmpty();
    }

    private boolean areAllListInfosSelectable(ArrayList<ListView.FixedViewInfo> infos) {
        if (infos == null) {
            return true;
        }
        Iterator<ListView.FixedViewInfo> it = infos.iterator();
        while (it.hasNext()) {
            if (!it.next().isSelectable) {
                return false;
            }
        }
        return true;
    }

    public boolean removeHeader(View v) {
        int i = 0;
        while (true) {
            boolean z = false;
            if (i >= this.mHeaderViewInfos.size()) {
                return false;
            }
            if (this.mHeaderViewInfos.get(i).view == v) {
                this.mHeaderViewInfos.remove(i);
                if (areAllListInfosSelectable(this.mHeaderViewInfos) && areAllListInfosSelectable(this.mFooterViewInfos)) {
                    z = true;
                }
                this.mAreAllFixedViewsSelectable = z;
                return true;
            }
            i++;
        }
    }

    public boolean removeFooter(View v) {
        int i = 0;
        while (true) {
            boolean z = false;
            if (i >= this.mFooterViewInfos.size()) {
                return false;
            }
            if (this.mFooterViewInfos.get(i).view == v) {
                this.mFooterViewInfos.remove(i);
                if (areAllListInfosSelectable(this.mHeaderViewInfos) && areAllListInfosSelectable(this.mFooterViewInfos)) {
                    z = true;
                }
                this.mAreAllFixedViewsSelectable = z;
                return true;
            }
            i++;
        }
    }

    @Override // android.widget.Adapter
    public int getCount() {
        if (this.mAdapter != null) {
            return getFootersCount() + getHeadersCount() + this.mAdapter.getCount();
        }
        return getFootersCount() + getHeadersCount();
    }

    @Override // android.widget.ListAdapter
    public boolean areAllItemsEnabled() {
        ListAdapter listAdapter = this.mAdapter;
        if (listAdapter == null) {
            return true;
        }
        if (!this.mAreAllFixedViewsSelectable || !listAdapter.areAllItemsEnabled()) {
            return false;
        }
        return true;
    }

    @Override // android.widget.ListAdapter
    public boolean isEnabled(int position) {
        int numHeaders = getHeadersCount();
        if (position < numHeaders) {
            return this.mHeaderViewInfos.get(position).isSelectable;
        }
        int adjPosition = position - numHeaders;
        int adapterCount = 0;
        ListAdapter listAdapter = this.mAdapter;
        if (listAdapter != null && adjPosition < (adapterCount = listAdapter.getCount())) {
            return this.mAdapter.isEnabled(adjPosition);
        }
        if (adjPosition - adapterCount < this.mFooterViewInfos.size()) {
            return this.mFooterViewInfos.get(adjPosition - adapterCount).isSelectable;
        }
        Log.e(TAG, "throw an IndexOutOfBoundsException", new IndexOutOfBoundsException());
        if (this.mFooterViewInfos.size() <= 0) {
            return false;
        }
        ArrayList<ListView.FixedViewInfo> arrayList = this.mFooterViewInfos;
        return arrayList.get(arrayList.size() - 1).isSelectable;
    }

    @Override // android.widget.Adapter
    public Object getItem(int position) {
        int numHeaders = getHeadersCount();
        if (position < numHeaders) {
            return this.mHeaderViewInfos.get(position).data;
        }
        int adjPosition = position - numHeaders;
        int adapterCount = 0;
        ListAdapter listAdapter = this.mAdapter;
        if (listAdapter != null && adjPosition < (adapterCount = listAdapter.getCount())) {
            return this.mAdapter.getItem(adjPosition);
        }
        if (adjPosition - adapterCount < this.mFooterViewInfos.size()) {
            return this.mFooterViewInfos.get(adjPosition - adapterCount).data;
        }
        Log.e(TAG, "throw an IndexOutOfBoundsException", new IndexOutOfBoundsException());
        if (this.mFooterViewInfos.size() <= 0) {
            return null;
        }
        ArrayList<ListView.FixedViewInfo> arrayList = this.mFooterViewInfos;
        return arrayList.get(arrayList.size() - 1).data;
    }

    @Override // android.widget.Adapter
    public long getItemId(int position) {
        int adjPosition;
        int numHeaders = getHeadersCount();
        ListAdapter listAdapter = this.mAdapter;
        if (listAdapter == null || position < numHeaders || (adjPosition = position - numHeaders) >= listAdapter.getCount()) {
            return -1;
        }
        return this.mAdapter.getItemId(adjPosition);
    }

    @Override // android.widget.Adapter
    public boolean hasStableIds() {
        ListAdapter listAdapter = this.mAdapter;
        if (listAdapter != null) {
            return listAdapter.hasStableIds();
        }
        return false;
    }

    @Override // android.widget.Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        int numHeaders = getHeadersCount();
        if (position < numHeaders) {
            return this.mHeaderViewInfos.get(position).view;
        }
        int adjPosition = position - numHeaders;
        int adapterCount = 0;
        ListAdapter listAdapter = this.mAdapter;
        if (listAdapter != null && adjPosition < (adapterCount = listAdapter.getCount())) {
            return this.mAdapter.getView(adjPosition, convertView, parent);
        }
        if (adjPosition - adapterCount < this.mFooterViewInfos.size()) {
            return this.mFooterViewInfos.get(adjPosition - adapterCount).view;
        }
        Log.e(TAG, "throw an IndexOutOfBoundsException", new IndexOutOfBoundsException());
        if (this.mFooterViewInfos.size() <= 0) {
            return null;
        }
        ArrayList<ListView.FixedViewInfo> arrayList = this.mFooterViewInfos;
        return arrayList.get(arrayList.size() - 1).view;
    }

    @Override // android.widget.Adapter
    public int getItemViewType(int position) {
        int adjPosition;
        int numHeaders = getHeadersCount();
        ListAdapter listAdapter = this.mAdapter;
        if (listAdapter == null || position < numHeaders || (adjPosition = position - numHeaders) >= listAdapter.getCount()) {
            return -2;
        }
        return this.mAdapter.getItemViewType(adjPosition);
    }

    @Override // android.widget.Adapter
    public int getViewTypeCount() {
        ListAdapter listAdapter = this.mAdapter;
        if (listAdapter != null) {
            return listAdapter.getViewTypeCount();
        }
        return 1;
    }

    @Override // android.widget.Adapter
    public void registerDataSetObserver(DataSetObserver observer) {
        ListAdapter listAdapter = this.mAdapter;
        if (listAdapter != null) {
            listAdapter.registerDataSetObserver(observer);
        }
    }

    @Override // android.widget.Adapter
    public void unregisterDataSetObserver(DataSetObserver observer) {
        ListAdapter listAdapter = this.mAdapter;
        if (listAdapter != null) {
            listAdapter.unregisterDataSetObserver(observer);
        }
    }

    @Override // android.widget.Filterable
    public Filter getFilter() {
        if (this.mIsFilterable) {
            return ((Filterable) this.mAdapter).getFilter();
        }
        return null;
    }

    @Override // android.widget.WrapperListAdapter
    public ListAdapter getWrappedAdapter() {
        return this.mAdapter;
    }
}
