package com.koncle.imagemanagement.view.timelineview;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

/**
 * Created by Koncle on 2018/3/19.
 */

public class TimeLineImageView extends ViewPager {
    public static final float imageNum = 1;
    public static final float mid = 1 / imageNum;

    public TimeLineImageView(Context context) {
        super(context);
    }

    public TimeLineImageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}
