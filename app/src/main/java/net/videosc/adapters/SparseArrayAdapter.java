package net.videosc.adapters;

import android.util.SparseArray;
import android.widget.BaseAdapter;

public abstract class SparseArrayAdapter<E> extends BaseAdapter {
    private SparseArray<E> mData;

    public void setData(SparseArray<E> data) {
        this.mData = data;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public E getItem(int position) {
        return mData.valueAt(position);
    }

    @Override
    public long getItemId(int position) {
        return mData.keyAt(position);
    }
}
