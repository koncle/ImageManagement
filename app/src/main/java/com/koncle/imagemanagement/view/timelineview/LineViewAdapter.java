package com.koncle.imagemanagement.view.timelineview;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by Koncle on 2018/3/21.
 */

public abstract class LineViewAdapter<T> {
    private final Context context;
    private List<T> items;
    private OnDataSetChangedListener onDataSetChangedListener;

    public LineViewAdapter(Context context) {
        this.context = context;
    }

    public void setItems(List<T> items) {
        this.items = items;
        if (onDataSetChangedListener != null) {
            onDataSetChangedListener.onChanged();
        }
    }

    public T getItem(int positiion) {
        return items.get(positiion);
    }

    public int getCount() {
        return items.size();
    }

    public abstract View getView(ViewGroup parent, int pos);

    public void setOnDataSetChangedListener(OnDataSetChangedListener onDataSetChangedListener) {
        this.onDataSetChangedListener = onDataSetChangedListener;
    }
}
