package com.koncle.imagemanagement.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
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
    private final String TAG = getClass().getSimpleName();

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
            SubsamplingScaleImageView subsamplingScaleImageView = new SubsamplingScaleImageView(this.context);
            if (cur == position)
                subsamplingScaleImageView.setTransitionName(context.getString(R.string.m2s_transition));
            subsamplingScaleImageView.setImage(ImageSource.uri(path));
            subsamplingScaleImageView.setOnImageEventListener(new SubsamplingScaleImageView.OnImageEventListener() {
                @Override
                public void onReady() {
                }

                @Override
                public void onImageLoaded() {
                }

                @Override
                public void onPreviewLoadError(Exception e) {

                    Log.w(TAG, "Preview Load Error");
                    operator.addDeleteImage(image);
                }

                @Override
                public void onImageLoadError(Exception e) {
                    Toast.makeText(context, "This image has been deleted by other Appes", Toast.LENGTH_SHORT).show();
                    operator.addDeleteImage(image);
                    Log.w(TAG, "Load Error");
                }

                @Override
                public void onTileLoadError(Exception e) {
                    Log.w(TAG, "Title Load Error");
                }

                @Override
                public void onPreviewReleased() {
                }
            });
            subsamplingScaleImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    operator.toggleMode();
                }
            });

            container.addView(subsamplingScaleImageView);
            return subsamplingScaleImageView;

            /*
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
            */
        }
        return null;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((SubsamplingScaleImageView) object);
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
