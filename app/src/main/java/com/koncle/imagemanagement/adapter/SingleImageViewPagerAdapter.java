package com.koncle.imagemanagement.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.koncle.imagemanagement.bean.Image;

import java.util.List;

/**
 * Created by 10976 on 2018/1/10.
 */

public class SingleImageViewPagerAdapter extends PagerAdapter {
    private final List<Image> images;
    private final Context context;
    private int width;
    private ModeChange operator;

    public SingleImageViewPagerAdapter(Context context, List<Image> images) {
        this.images = images;
        this.context = context;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        this.width = wm.getDefaultDisplay().getWidth();

        Log.w(this.getClass().getSimpleName(), "DATA : " + images.get(0));
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        ImageView imageView = null;
        if (images.size() >= position) {
            Image image = images.get(position);
            String path = image.getPath();
            operator.changeTitle(image.getName());
            //imageView = new FullScreenImageView(this.context);
            imageView = new ImageView(this.context);
            Glide.with(this.context)
                    .load(path)
                    //   .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(imageView);

            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);  // 充满容器

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    operator.toggleMode();
                }
            });

            container.addView(imageView);
        }
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((ImageView) object);
    }

    public void setOperator(ModeChange operator) {
        this.operator = operator;
    }

    public interface ModeChange {
        void toggleMode();

        void changeTitle(String s);
    }

    // Another way to show image
    class FullScreenImageView extends android.support.v7.widget.AppCompatImageView {
        public FullScreenImageView(Context context) {
            super(context);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            Drawable drawable = getDrawable();

            if (drawable != null) {
                int width = MeasureSpec.getSize(widthMeasureSpec);
                // Get the displayed height of the image.
                // Ceil not round - avoid thin vertical gaps along the left/right edges
                int height = (int) Math.ceil(
                        (float) width * drawable.getIntrinsicHeight() / drawable.getIntrinsicWidth());
                setMeasuredDimension(width, height);
            } else {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        }
    }
}
