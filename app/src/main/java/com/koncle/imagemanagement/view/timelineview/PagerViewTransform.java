package com.koncle.imagemanagement.view.timelineview;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by Koncle on 2018/3/19.
 */

public class PagerViewTransform implements ViewPager.PageTransformer {

    @Override
    public void transformPage(View view, float position) {
        /*
        float scale = 0.5f;
        float fixvalue = 200f;

        // adjust position which is 0.3 off the normal position
        position -= TimeLineView.mid;

        float scaleValue;
        if (position > -TimeLineView.mid && position < TimeLineView.mid) {
            scaleValue = 1 - Math.abs(position);
        }else{
            scaleValue = 1 - TimeLineView.mid;
            view.setElevation(scaleValue);
        }

        scaleValue *= scaleValue;
        view.setScaleX(scaleValue);
        view.setScaleY(scaleValue);

        //view.setPivotX(position < 0 ? view.getWidth() * 0.25f : -(view.getWidth() * 0.75f));
        //view.setPivotX(view.getWidth() * (1 - position - (position > 0 ? 1 : -1) * 0.75f) * scale);
        //view.setElevation(position > -0.25 && position < 0.25 ? 1 : -1);
        */

        float scale = 0.5f;
        float scaleValue;
        if (position >= -2.0 && position <= 2.0) {
            scaleValue = 1 - Math.abs(position) * scale;
        } else {
            scaleValue = 0;
        }
        scaleValue = sinx(scaleValue);
        view.setScaleX(scaleValue);
        view.setScaleY(scaleValue);
        view.setAlpha(scaleValue);

        // fix the problem that image that has disappeared appears again
        float pivotx = view.getWidth() * (1 - position - (position > 0 ? 1 : -1) * 0.75f) * scale;
        view.setPivotX(pivotx);

        // fix the problem that when swip images quickly,
        // some image will appear from the top of the viewpager, not the center
        view.setPivotY(view.getHeight() / 2);
        view.setElevation(position > -0.25 && position < 0.25 ? 1 : 0);

        /*
        TextView text = view.findViewById(R.id.timeline_time);
        text.setText(view.getWidth() + " " + pivotx+"");
        */
    }

    private float sinx(float x) {
        return (float) ((Math.sin((x - 0.5f) * Math.PI) + 1) / 2);
    }

    private float sigmoid(float x) {
        return (float) (1 / (1 + Math.pow(Math.E, -((x - 0.5) * 20))));
    }
}
