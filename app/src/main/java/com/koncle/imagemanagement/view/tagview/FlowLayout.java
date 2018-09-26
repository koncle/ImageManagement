package com.koncle.imagemanagement.view.tagview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Koncle on 2018/1/30.
 */

public class FlowLayout extends ViewGroup {


    private List<List<View>> views = new ArrayList<>();

    private int lines = 0;

    private List<Integer> lineHeights = new ArrayList<>();

    public FlowLayout(Context context) {
        super(context);
        init();
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    // control children margin
    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    public void init() {
        for (int i = 0; i < 10; i++) {
            List<View> lineViews = new ArrayList<>();
            views.add(lineViews);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // trigger children's onMeasure() method to get their size

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int childCount = getChildCount();

        measureChildren(widthMeasureSpec, heightMeasureSpec);

        int height = 0;
        int width = 0;

        int lineHeight = 0;
        int lineWidth = 0;
        int index;
        for (index = 0; index < childCount; index++) {
            View child = getChildAt(index);
            // get child size

            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            int childWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            int childHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            Log.w("FlowLayout", "chlid : " + lines + " " + childWidth + " " + childHeight);
            if (lineWidth + childWidth >= widthSize) {
                width = Math.max(lineWidth, childWidth);
                lineWidth = childWidth;
                height += lineHeight;
                lineHeight = Math.max(lineHeight, childHeight);
            } else {
                lineWidth += childWidth;
                lineHeight = Math.max(lineHeight, childHeight);
            }
        }

        if (index == childCount) {
            width = Math.max(width, lineWidth);
            height += lineHeight;
        }

        width += getPaddingStart() + getPaddingRight();
        height += getPaddingTop() + getPaddingBottom();

        setMeasuredDimension(widthMode == MeasureSpec.EXACTLY ? widthSize : width,
                heightMode == MeasureSpec.EXACTLY ? heightSize : height);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0; i < views.size(); i++) {
            views.get(i).clear();
        }
        lineHeights.clear();
        lines = 0;
        int childCount = getChildCount();

        int width = getMeasuredWidth();

        int lineHeight = 0;
        int lineWidth = 0;
        int paddingEnd = getPaddingEnd();
        int index;
        View child = null;
        List<View> lineViews = views.get(0);
        for (index = 0; index < childCount; index++) {
            child = getChildAt(index);
            // get child size
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            int childWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            int childHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            Log.w("FlowLayout", "chlid : " + lines + " " + childWidth + " " + childHeight);
            if (lineWidth + childWidth + paddingEnd >= width) {
                lineWidth = childWidth;
                lines += 1;
                if (lines > views.size() - 1) {
                    lineViews = new ArrayList<>();
                    views.add(lineViews);
                } else {
                    lineViews = views.get(lines);
                }
                lineHeights.add(lineHeight);
            } else {
                lineWidth += childWidth;
                lineHeight = Math.max(lineHeight, childHeight);
            }
            lineViews.add(child);
        }

        lineHeights.add(lineHeight);
        lines += 1;

        int left = getPaddingLeft();
        int top = getPaddingTop();
        for (int i = 0; i < lines; i++) {
            List<View> viewsInLine = views.get(i);
            lineHeight = lineHeights.get(i);

            for (int j = 0; j < viewsInLine.size(); j++) {
                child = viewsInLine.get(j);
                if (child.getVisibility() == GONE) {
                    continue;
                }
                MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                // child params
                int lc = left + lp.leftMargin;
                int tc = top + lp.topMargin;
                int rc = lc + child.getMeasuredWidth();
                int bc = tc + child.getMeasuredHeight();

                child.layout(lc, tc, rc, bc);

                left += lp.leftMargin + child.getMeasuredWidth() + lp.rightMargin;
            }
            // new line
            left = 0;
            top += lineHeight;
        }
    }


}
