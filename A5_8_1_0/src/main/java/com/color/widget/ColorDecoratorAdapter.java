package com.color.widget;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

public abstract class ColorDecoratorAdapter extends BaseAdapter implements Filterable {
    private static final boolean DBG = false;
    private static final String TAG = "ColorDecoratorAdapter";
    protected BaseAdapter mAdapter = null;
    private boolean mIsFilterable = DBG;

    public ColorDecoratorAdapter(BaseAdapter adapter) {
        setAdapter(adapter);
    }

    public boolean hasStableIds() {
        return this.mAdapter.hasStableIds();
    }

    public void registerDataSetObserver(DataSetObserver observer) {
        this.mAdapter.registerDataSetObserver(observer);
    }

    public void unregisterDataSetObserver(DataSetObserver observer) {
        this.mAdapter.unregisterDataSetObserver(observer);
    }

    public void notifyDataSetChanged() {
        this.mAdapter.notifyDataSetChanged();
    }

    public void notifyDataSetInvalidated() {
        this.mAdapter.notifyDataSetInvalidated();
    }

    public boolean areAllItemsEnabled() {
        return this.mAdapter.areAllItemsEnabled();
    }

    public boolean isEnabled(int position) {
        return this.mAdapter.isEnabled(position);
    }

    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return this.mAdapter.getDropDownView(position, convertView, parent);
    }

    public int getItemViewType(int position) {
        return this.mAdapter.getItemViewType(position);
    }

    public int getViewTypeCount() {
        return this.mAdapter.getViewTypeCount();
    }

    public boolean isEmpty() {
        return this.mAdapter.isEmpty();
    }

    public int getCount() {
        return this.mAdapter.getCount();
    }

    public Object getItem(int position) {
        return this.mAdapter.getItem(position);
    }

    public long getItemId(int position) {
        return this.mAdapter.getItemId(position);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        return this.mAdapter.getView(position, convertView, parent);
    }

    public Filter getFilter() {
        if (this.mIsFilterable) {
            return ((Filterable) this.mAdapter).getFilter();
        }
        return null;
    }

    public BaseAdapter getAdapter() {
        return this.mAdapter;
    }

    void setAdapter(BaseAdapter adapter) {
        this.mAdapter = adapter;
        this.mIsFilterable = this.mAdapter instanceof Filterable;
    }
}
