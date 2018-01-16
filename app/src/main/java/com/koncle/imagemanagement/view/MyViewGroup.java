package com.koncle.imagemanagement.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by 10976 on 2018/1/16.
 */

public class MyViewGroup extends ViewGroup {
    public MyViewGroup(Context context) {
        super(context);
    }

    public MyViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();

        int curHeight = t;

        for (int i = 0; i < count; ++i) {
            View child = getChildAt(i);
            int height = child.getMeasuredHeight();
            int width = child.getMeasuredWidth();
            child.layout(l, curHeight, l + width, curHeight + height);
            curHeight += height;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // trigger children's onMeasure() method to get their size
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int childCount = getChildCount();

        if (childCount == 0) {
            setMeasuredDimension(10, 10);
        } else {
            if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
                int height = getTotleHeight();
                int width = getMaxChildWidth();
            } else if (heightMode == MeasureSpec.AT_MOST) {
                setMeasuredDimension(widthSize, getTotleHeight());
            } else if (widthMode == MeasureSpec.AT_MOST) {
                setMeasuredDimension(getMaxChildWidth(), heightSize);
            }
        }
    }

    private int getMaxChildWidth() {
        int count = getChildCount();
        int width = 0;
        for (int i = 0; i < count; ++i) {
            View childView = getChildAt(i);
            if (childView.getMeasuredWidth() > width)
                width = childView.getMeasuredWidth();
        }
        return width;
    }

    private int getTotleHeight() {
        int count = getChildCount();
        int height = 0;
        for (int i = 0; i < count; ++i) {
            View childView = getChildAt(i);
            height += childView.getMeasuredHeight();
        }
        return height;
    }
}
