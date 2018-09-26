package com.koncle.imagemanagement.view.timelineview;

import android.support.v4.view.ViewPager;

import java.util.List;

/**
 * Created by Koncle on 2018/3/22.
 */

public class LineViewWrapperLayout<T> {
    private TimeLineImageView timeLineImageView;
    private TimeLineLineView timeLineLineView;
    private ImageViewAdapter imageViewAdapter;
    private LineViewAdapter lineViewAdapter;
    private List<T> items;

    public LineViewWrapperLayout(TimeLineImageView timeLineImageView, TimeLineLineView timeLineLineView) {
        this.timeLineImageView = timeLineImageView;
        this.timeLineLineView = timeLineLineView;
    }

    public void setItems(List<T> items) {
        this.items = items;
        imageViewAdapter.setItems(items);
        lineViewAdapter.setItems(items);
    }

    public void init(ImageViewAdapter<T> imageViewAdapter, LineViewAdapter<T> lineViewAdapter, List<T> items) {
        this.imageViewAdapter = imageViewAdapter;
        this.lineViewAdapter = lineViewAdapter;

        setItems(items);

        timeLineLineView.setAdapter(lineViewAdapter);
        timeLineImageView.setAdapter(imageViewAdapter);

        timeLineImageView.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // when viewpager moved, the time on timeline will move together
                timeLineLineView.movePoints(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                timeLineLineView.setCurPos(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
        timeLineLineView.setOnTimeLineMoveListener(new TimeLineLineView.OnTimeLineMoveListener() {
            @Override
            public void onMoved(float x) {
                //TODO: conbine two components
            }
        });
    }
}
