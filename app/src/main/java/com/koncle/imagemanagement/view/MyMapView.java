package com.koncle.imagemanagement.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;

import com.amap.api.maps.MapView;

/**
 * Created by 10976 on 2018/1/16.
 */

public class MyMapView extends MapView {
    public MyMapView(Context context) {
        super(context);
    }

    public MyMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.w("MyMapView", "onMeasure()");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.w("MyMapView", "onDraw()");

    }
}
