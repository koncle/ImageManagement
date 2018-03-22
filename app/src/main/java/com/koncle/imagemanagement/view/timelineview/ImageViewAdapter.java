package com.koncle.imagemanagement.view.timelineview;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by Koncle on 2018/3/19.
 */

public abstract class ImageViewAdapter<T> extends PagerAdapter {

    private final Context context;
    private List<T> items;
    private OnDataSetChangedListener onDataSetChangedListener;

    public ImageViewAdapter(Context context) {
        this.context = context;
    }

    public void setItems(List<T> images) {
        this.items = images;
    }

    public T getItem(int pos) {
        return this.items.get(pos);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public abstract View instantiateItem(ViewGroup container, int position);

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public float getPageWidth(int position) {
        return TimeLineImageView.mid;
    }

    public void setOnDataSetChangedListener(OnDataSetChangedListener onDataSetChangedListener) {
        this.onDataSetChangedListener = onDataSetChangedListener;
    }
}
