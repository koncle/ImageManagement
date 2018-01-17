package com.koncle.imagemanagement.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.koncle.imagemanagement.R;
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
    int cur;

    public SingleImageViewPagerAdapter(Context context, List<Image> images, int cur) {
        this.images = images;
        this.context = context;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        this.width = wm.getDefaultDisplay().getWidth();
        this.cur = cur;
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
        if (images.size() >= position) {

            final Image image = images.get(position);
            String path = image.getPath();

            //imageView = new FullScreenImageView(this.context);
            final ImageView imageView = new ImageView(this.context);

            if (cur == position)
                imageView.setTransitionName(context.getString(R.string.m2s_transition));

            Glide.with(this.context)
                    .load(path)
                    //   .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .error(R.drawable.error)
                    .into(new SimpleTarget<GlideDrawable>(imageView.getMaxWidth(), imageView.getMaxHeight()) {
                        @Override
                        public void onResourceReady(GlideDrawable glideDrawable, GlideAnimation<? super GlideDrawable> glideAnimation) {
                            imageView.setImageDrawable(glideDrawable.getCurrent());
                        }

                        @Override
                        public void onLoadFailed(Exception e, Drawable errorDrawable) {
                            operator.addDeleteImage(image);
                        }
                    });

            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);  // 充满容器


            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    operator.toggleMode();
                }
            });

            container.addView(imageView);
            return imageView;
        }
        return null;
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

        void deleteImage(Image image);

        void addDeleteImage(Image image);
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
