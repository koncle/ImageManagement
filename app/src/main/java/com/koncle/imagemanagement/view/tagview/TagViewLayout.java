package com.koncle.imagemanagement.view.tagview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.koncle.imagemanagement.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 10976 on 2018/1/16.
 */

public class TagViewLayout extends FlowLayout implements TagAdapter.OnDataChangedListener {
    private TagAdapter tagAdapter;
    private MotionEvent motionEvent;
    private OnTagClickListener onTagClickListener;

    private boolean supportMulSelected;
    private int selectMaxNum;
    private boolean canSelect;

    private List<View> selectedViews;
    private OnSelectStateChangeListener onSelectStateChangeListener;

    public TagViewLayout(Context context) {
        super(context);
    }

    public TagViewLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TagViewLayout);

        supportMulSelected = typedArray.getBoolean(R.styleable.TagViewLayout_canMultiSelect, false);
        canSelect = typedArray.getBoolean(R.styleable.TagViewLayout_canSelect, false);
        selectMaxNum = typedArray.getInteger(R.styleable.TagViewLayout_selectMaxNum, 0);
        typedArray.recycle();

        selectedViews = new ArrayList<>();
    }

    public void setAdapter(TagAdapter adapter) {
        this.tagAdapter = adapter;
        tagAdapter.setOnDataChangeListener(this);
        changeAdapter();
    }

    public void onChanged() {
        selectedViews.clear();
        changeAdapter();
    }

    private void changeAdapter() {
        removeAllViews();
        TagView tagViewContainer = null;
        for (int i = 0; i < tagAdapter.getCount(); i++) {
            View tagView = tagAdapter.getView(this, i, tagAdapter.getItem(i));

            tagViewContainer = new TagView(getContext());
            //tagViewContainer.setDuplicateParentStateEnabled(true);
            tagView.setDuplicateParentStateEnabled(true);

            // copy child's params to container
            if (tagView.getLayoutParams() != null) {
                tagViewContainer.setLayoutParams(tagView.getLayoutParams());
            } else {
                MarginLayoutParams lp = new MarginLayoutParams(
                        LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT);

                lp.setMargins(dip2px(getContext(), 5),
                        dip2px(getContext(), 5),
                        dip2px(getContext(), 5),
                        dip2px(getContext(), 5));
                tagViewContainer.setLayoutParams(lp);
            }
            // reset child's params
            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            tagView.setLayoutParams(lp);

            tagViewContainer.addView(tagView);
            addView(tagViewContainer);
        }
    }

    private int dip2px(Context context, int dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            motionEvent = MotionEvent.obtain(event);
            performClick();
        }
        // continue process event
        return true;
    }

    @Override
    public boolean performClick() {
        if (motionEvent == null) return super.performClick();

        int x = (int) motionEvent.getX();
        int y = (int) motionEvent.getY();
        motionEvent = null;

        TagView child = findChild(x, y);
        int pos = findPosByView(child);
        if (child != null) {
            if (onTagClickListener != null) {
                // user didn't cancel the event and the attr of canSelect is set to be true
                if (!onTagClickListener.onTagClick(child.getChildView(), pos) && canSelect) {
                    // then select the item
                    doSelect(child, pos);
                    super.performClick();
                }
            }
        }
        return super.performClick();
    }

    private void doSelect(TagView child, int pos) {
        if (supportMulSelected) {
            if (!child.isChecked()) {
                if (selectMaxNum > 0) {
                    if (selectedViews.size() >= selectMaxNum) {
                        return;
                    } else {
                        child.setChecked(true);
                        selectedViews.add(child);
                    }
                }
                // checked
            } else {
                child.setChecked(false);
                selectedViews.remove(child);
            }
        } else {
            selectedViews.clear();
            child.toggle();
        }
        if (onSelectStateChangeListener != null)
            onSelectStateChangeListener.onSelectedStateChange(child.getChildView(), pos, child.isChecked());
    }

    private int findPosByView(TagView child) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            TagView v = (TagView) getChildAt(i);
            if (v == child) return i;
        }
        return -1;
    }

    /*
    * find a child by its position
    * */
    private TagView findChild(int x, int y) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            TagView v = (TagView) getChildAt(i);
            if (v.getVisibility() == View.GONE) continue;
            Rect outRect = new Rect();
            v.getHitRect(outRect);
            if (outRect.contains(x, y)) return v;
        }
        return null;
    }

    public boolean isSupportMulSelected() {
        return supportMulSelected;
    }

    public void setSupportMulSelected(boolean supportMulSelected) {
        this.supportMulSelected = supportMulSelected;
    }

    public void setSelectMaxNum(int selectMaxNum) {
        this.selectMaxNum = selectMaxNum;
    }

    public int getSelectMaxNum() {
        return selectMaxNum;
    }

    public List<View> getSelectedViews() {
        return selectedViews;
    }

    public void setOnTagClickListener(OnTagClickListener onTagClickListener) {
        this.onTagClickListener = onTagClickListener;
    }

    public void setOnSelectStateChangeListener(OnSelectStateChangeListener onSelectStateChangeListener) {
        this.onSelectStateChangeListener = onSelectStateChangeListener;
    }

    public interface OnTagClickListener {
        /*
        * Handle the event of click
        *
        * @return : if true, then cancel select tag
        * */
        boolean onTagClick(View view, int position);
    }

    public interface OnSelectStateChangeListener {
        /*
        * When tag's state changed, it will be called
        * */
        void onSelectedStateChange(View view, int position, boolean checked);
    }
}
