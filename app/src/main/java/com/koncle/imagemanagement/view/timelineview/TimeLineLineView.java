package com.koncle.imagemanagement.view.timelineview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.NinePatch;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.koncle.imagemanagement.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 10976 on 2018/1/16.
 */

public class TimeLineLineView extends ViewGroup implements OnDataSetChangedListener {

    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    private static final int INTERVAL = 50;
    private static final int BOTTOM_LINE_HEIGHT = 80;
    private static final boolean DEBUG = false;
    private final int showNum = 3;
    private final int pointRadius = 20;
    private int defaultSize;
    private float moveX = 100.f;
    private float moveY = 100.f;
    private float offsetX = 0.f;
    private float offsetY = 0.f;
    private Paint paint;
    private List<Point> points;
    private int pointsNum = 3;
    private int curPos;
    private float preOffset;
    private ViewPager viewPager;
    private GestureDetector gestureDetector;

    private int maxChildWidth;
    private int maxChildHeight;
    private LineViewAdapter lineViewAdapter;
    private Bitmap timeLineBitmap;
    private NinePatch ninePatch;
    private OnTimeLineMoveListener onTimeLineMoveListener;

    private GestureDetector.OnGestureListener onGestureListener = new GestureDetector.OnGestureListener() {
        // must return true to give this event to fling()
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float dist = e2.getX() - e1.getX();
            if (dist > 0) {
                moveTo(LEFT, dist);
            } else {
                moveTo(RIGHT, dist);
            }
            return true;
        }
    };

    public TimeLineLineView(Context context) {
        super(context);
        initParams();
    }

    public TimeLineLineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initParams();
    }

    @Override
    public void onChanged() {
        pointsNum = lineViewAdapter.getCount();
        initPoints();
        invalidate();
    }

    public LineViewAdapter getAdapter() {
        return lineViewAdapter;
    }

    public void setAdapter(LineViewAdapter lineViewAdapter) {
        this.lineViewAdapter = lineViewAdapter;
        lineViewAdapter.setOnDataSetChangedListener(this);
        // dataset changed
        onChanged();
    }

    private void initParams() {
        paint = new Paint();
        curPos = 0;
        gestureDetector = new GestureDetector(getContext(), onGestureListener);
    }

    private void initPoints() {
        points = new ArrayList<>();
        for (int i = 0; i < pointsNum; ++i) {
            points.add(new Point());

            View view = lineViewAdapter.getView(this, i);
            //LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            //view.setLayoutParams(lp);

            view.setBackgroundColor(Color.WHITE);
            this.addView(view);
        }
    }

    public void movePoints(int position, float positionOffset, int positionOffsetPixels) {
        offsetX = -(getMeasuredWidth() - maxChildWidth) / (showNum - 1) * (position + positionOffset);

        //float curOffset = position + positionOffset;
        //Log.e("offsets",  " preoffset " + preOffset + " curOffset " + curOffset + "  distance  " + distance);

        //preOffset = curOffset;
        invalidate();
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
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        int count = getChildCount();
        maxChildWidth = 0;
        maxChildHeight = 0;
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getMeasuredWidth() > maxChildWidth) {
                maxChildWidth = child.getMeasuredWidth();
            }
            if (child.getMeasuredHeight() > maxChildHeight) {
                maxChildHeight = child.getMeasuredHeight();
            }
        }

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        width = widthMode == MeasureSpec.EXACTLY ? width : maxChildWidth * 3 + INTERVAL;
        height = heightMode == MeasureSpec.EXACTLY ? height : maxChildHeight;

        setMeasuredDimension(width, height);

        if (DEBUG) {
            Log.w(this.toString(), "onMeasure :  width :" + width + " height : " + height);
        }
        // initialize points
        float distance = (width - maxChildWidth) / (showNum - 1.f);
        float startX = maxChildWidth / 2 + distance;
        for (int i = 0; i < pointsNum; ++i) {
            points.get(i).setPoint(startX, 0);
            startX += distance;
        }
    }

    private void moveTo(int direction, float offset) {
        Log.e("move", "" + offset + " " + direction);
        onTimeLineMoveListener.onMoved(offset);
        int count = getChildCount();
        switch (direction) {
            case LEFT:
                break;
            case RIGHT:
                break;
        }
        /*
        for (int i = 0; i < count; i++) {
            getChildAt(i).scrollTo((int) offset, 0);
        }
        */
        //Scroller scroller = new Scroller(getContext());
        //scroller.startScroll(getScrollX(), getScrollY(), (int) offset, 0, 2);
        //this.scrollBy((int) offset, 0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        /*
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                moveX = event.getX();
                moveY = event.getY();
                if (DEBUG) {
                    Log.e("action", "movex : " + moveX + " moveY : " + moveY);
                }
                //scrollTo((int)moveX, (int)moveY);
                break;
            case MotionEvent.ACTION_MOVE:
                offsetX = event.getX() - moveX;
                offsetY = event.getY() - moveY;
                if (DEBUG) {
                    Log.e("action", "offsetx : " + offsetX + " offsetY : " + offsetY);
                }
                invalidate();

                break;
            case MotionEvent.ACTION_UP:
                offsetX = event.getX() - moveX;
                offsetY = event.getY() - moveY;
                if (offsetX > getMeasuredWidth() / 2){
                    //scrollTo();
                }
                for (int i = 0; i < pointsNum; i++) {
                    points.get(i).addOffset(offsetX);
                }
                if (DEBUG) {
                    Log.e("action", "up");
                }
                break;
        }

        return true;
        */
        return gestureDetector.onTouchEvent(event);
    }


    @Override
    protected void dispatchDraw(Canvas canvas) {
        Log.e("dispatchDraw", " ");
        super.dispatchDraw(canvas);

        // draw children
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            drawPoint(canvas, i);
        }

        // draw a line
        drawTimeLine(canvas);
    }

    private void drawTimeLine(Canvas canvas) {
        int width = getMeasuredWidth();
        float centerY = maxChildHeight;

        paint.setColor(Color.GRAY);

        // draw nine patch file
        if (timeLineBitmap == null) {
            timeLineBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ar2);
            ninePatch = new NinePatch(timeLineBitmap, timeLineBitmap.getNinePatchChunk(), null);
        }

        // remove white space in the image
        // so draw from -10 to width + 10
        Rect rect = new Rect(-10, (int) centerY - 25, width + 10, (int) centerY + 25);

        ninePatch.draw(canvas, rect, paint);
    }

    public void setCurPos(int pos) {
        this.curPos = pos;
    }

    private void drawPoint(Canvas canvas, int pos) {
        View child = getChildAt(pos);
        Point point = points.get(pos);
        float x = getMovedPointX(point, offsetX);
        float y = point.getY();
        if (x + child.getMeasuredWidth() < 0 || x > getMeasuredWidth()) return;
        canvas.save();
        canvas.translate(x - child.getMeasuredWidth() / 2, y);
        child.draw(canvas);
        canvas.restore();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            child.layout(l + i * maxChildWidth, t, l + (i + 1) * maxChildWidth, b);
            Log.e("layout", " " + (i * maxChildWidth));
        }
    }

    private float getMovedPointX(Point point, float offset) {
        int width = getMeasuredWidth();
        float pos = point.getX() + point.getOffset() + offset;
        //if (pos < maxChildWidth) return maxChildWidth;
        //if (pos > width-maxChildWidth) return width-maxChildWidth;

        if (DEBUG) {
            Log.e("pos", point.getX() + " " + pos + "");
        }
        return pos;
    }

    public void setOnTimeLineMoveListener(OnTimeLineMoveListener onTimeLineMoveListener) {
        this.onTimeLineMoveListener = onTimeLineMoveListener;
    }

    interface OnTimeLineMoveListener{
        void onMoved(float x);
    }


    private class Point {
        private float x;
        private float y;
        private float offset;

        public void setPoint(float x, float y) {
            this.setPoint(x, y, 0.f);
        }

        public void setPoint(float x, float y, float offset) {
            this.x = x;
            this.y = y;
            this.offset = offset;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public float getOffset() {
            return offset;
        }

        public void setOffset(float offset) {
            this.offset = offset;
        }

        public void addOffset(float offset) {
            this.offset += offset;
        }
    }
}
