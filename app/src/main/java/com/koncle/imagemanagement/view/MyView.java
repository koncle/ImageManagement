package com.koncle.imagemanagement.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.koncle.imagemanagement.R;

/**
 * Created by 10976 on 2018/1/16.
 */

public class MyView extends ImageView {
    private int defaultSize;

    public MyView(Context context) {
        super(context);
    }

    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MyView);

        defaultSize = typedArray.getDimensionPixelSize(R.styleable.MyView_default_size, 100);

        // recollect object
        typedArray.recycle();
    }

    private int getSize(int measureSpec, int defaultSize) {
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);

        switch (mode) {
            case MeasureSpec.UNSPECIFIED:
                size = defaultSize;
                break;
            case MeasureSpec.AT_MOST:
                size = size;
                break;
            case MeasureSpec.EXACTLY:
                size = size;
                break;
        }
        return size;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = getSize(heightMeasureSpec, defaultSize);
        int width = getSize(widthMeasureSpec, defaultSize);

        if (width > height) {
            width = height;
        } else {
            height = width;
        }
        setMeasuredDimension(width, height);
        Log.w(this.toString(), "onMeasure :  size :" + width);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int r = getMeasuredHeight() / 2;
        int centerX = r;
        int centerY = r;

        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        canvas.drawCircle(centerX, centerY, r, paint);
    }

}
