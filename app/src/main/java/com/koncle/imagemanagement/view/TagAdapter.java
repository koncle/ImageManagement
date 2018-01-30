package com.koncle.imagemanagement.view;

import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by Koncle on 2018/1/29.
 */

public abstract class TagAdapter<T> {
    private List<T> tagDatas;
    private TagViewLayout onDataChangeListener;

    public void setOnDataChangeListener(TagViewLayout onDataChangeListener) {
        this.onDataChangeListener = onDataChangeListener;
    }

    public void setTags(List<T> tags) {
        tagDatas = tags;
        notifyDataSetChanged();
    }

    public interface OnDataChangedListener {
        void onChanged();
    }

    public TagAdapter(List<T> datas) {
        tagDatas = datas;
    }

    public int getCount() {
        return tagDatas == null ? 0 : tagDatas.size();
    }

    public T getItem(int position) {
        return tagDatas == null ? null : tagDatas.get(position);
    }

    public void notifyDataSetChanged() {
        onDataChangeListener.onChanged();
    }

    public abstract View getView(ViewGroup parent, int position, T tag);
}
