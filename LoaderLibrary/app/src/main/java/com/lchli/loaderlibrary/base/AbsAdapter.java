package com.lchli.loaderlibrary.base;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

public abstract class AbsAdapter<T, VH extends AbsViewHolder> extends BaseAdapter {

    protected List<T> mDatas;

    public AbsAdapter(List<T> datas) {
        mDatas = new ArrayList<T>();
        if (datas != null) {
            mDatas.addAll(datas);
        }
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public final void remove(int index) {
        if (index < 0 || index >= mDatas.size())
            return;
        mDatas.remove(index);
        this.notifyDataSetChanged();

    }

    public final void remove(T data) {
        if (data == null)
            return;
        mDatas.remove(data);
        this.notifyDataSetChanged();

    }

    public final void add(T t) {
        if (t == null)
            return;
        mDatas.add(t);
        this.notifyDataSetChanged();
    }

    public final void refresh(List<T> datas) {
        List<T> temp = new ArrayList<T>();
        if (datas != null) {
            temp.addAll(datas);
        }
        mDatas = temp;
        this.notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            VH vh = onCreateViewHolder(parent, getItemViewType(position));
            onBindViewHolder(vh, position);
            vh.itemView.setTag(vh);
            return vh.itemView;
        } else {
            VH vh = (VH) convertView.getTag();
            onBindViewHolder(vh, position);
            return convertView;
        }
    }

    public abstract VH onCreateViewHolder(ViewGroup parent, int viewType);

    public abstract void onBindViewHolder(final VH holder, int position);


}
