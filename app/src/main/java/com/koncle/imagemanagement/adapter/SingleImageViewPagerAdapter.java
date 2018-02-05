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

import java.io.File;
import java.io.IOException;
import java.util.List;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * Created by 10976 on 2018/1/10.
 */

public class SingleImageViewPagerAdapter extends PagerAdapter {
    private final List<Image> images;
    private final Context context;
    private final String TAG = getClass().getSimpleName();
    int cur;
    private int width;
    private ModeChange operator;

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
    public Object instantiateItem(final ViewGroup container, final int position) {
        if (images.size() >= position) {
            final Image image = images.get(position);
            final String path = image.getPath();

            //imageView = new FullScreenImageView(this.context);
            View view;

            if (Image.TYPE_NORNAL == image.getType()) {
                final SubsamplingScaleImageView subsamplingScaleImageView = new SubsamplingScaleImageView(this.context);
                /*
                Glide.with(context)
                        .load(path)
                        .into(new SimpleTarget<GlideDrawable>() {
                            @Override
                            public void onResourceReady(GlideDrawable glideDrawable, GlideAnimation<? super GlideDrawable> glideAnimation) {
                            }
                        });
                        */
                subsamplingScaleImageView.setImage(ImageSource.uri(path));
                subsamplingScaleImageView.setOnImageEventListener(new SubsamplingScaleImageView.OnImageEventListener() {
                    int time = 0;
                    @Override
                    public void onReady() {
                    }

                    @Override
                    public void onImageLoaded() {
                    }

                    @Override
                    public void onPreviewLoadError(Exception e) {
                    }

                    @Override
                    public void onImageLoadError(Exception e) {
                        if (!new File(image.getPath()).exists()) {
                            Toast.makeText(context, "Invalid Image", Toast.LENGTH_SHORT).show();
                            operator.addDeleteImage(image);
                        } else {
                            if (time < 20) {
                                ++time;
                                Log.w(TAG, "Load Error " + time);
                                subsamplingScaleImageView.setImage(ImageSource.uri(path));
                            } else {
                                Toast.makeText(context, "Load error, can't load image", Toast.LENGTH_SHORT).show();
                                Log.w(TAG, "Load Error Finally");
                            }
                        }
                    }

                    @Override
                    public void onTileLoadError(Exception e) {
                    }

                    @Override
                    public void onPreviewReleased() {
                    }
                });
                view = subsamplingScaleImageView;
            } else {
                // GIF
                final GifImageView imageView = new GifImageView(this.context);
                try {
                    GifDrawable gifDrawable = new GifDrawable(image.getPath());
                    imageView.setImageDrawable(gifDrawable);
                } catch (IOException e) {
                    Log.w(TAG, " load gif image error " + image.getPath());
                    Toast.makeText(context, "This image has been deleted by other Appes", Toast.LENGTH_SHORT).show();
                    operator.addDeleteImage(image);
                }
                view = imageView;
            }

            if (cur == position) {
                view.setTransitionName(context.getString(R.string.m2s_transition));
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    operator.toggleMode();
                }
            });

            container.addView(view);
            return view;
        }// end if null
        return null;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
        if (object instanceof SubsamplingScaleImageView) {
            ((SubsamplingScaleImageView) object).recycle();
        }
    }

    public void setOperator(ModeChange operator) {
        this.operator = operator;
    }

    public interface ModeChange {
        void toggleMode();

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
