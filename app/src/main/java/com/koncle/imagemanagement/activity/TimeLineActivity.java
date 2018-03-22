package com.koncle.imagemanagement.activity;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.koncle.imagemanagement.R;
import com.koncle.imagemanagement.bean.Image;
import com.koncle.imagemanagement.dataManagement.ImageService;
import com.koncle.imagemanagement.util.ActivityUtil;
import com.koncle.imagemanagement.util.ImageUtils;
import com.koncle.imagemanagement.view.timelineview.ImageViewAdapter;
import com.koncle.imagemanagement.view.timelineview.LineViewAdapter;
import com.koncle.imagemanagement.view.timelineview.LineViewWrapperLayout;
import com.koncle.imagemanagement.view.timelineview.PagerViewTransform;
import com.koncle.imagemanagement.view.timelineview.TimeLineImageView;
import com.koncle.imagemanagement.view.timelineview.TimeLineLineView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Koncle on 2018/1/29.
 */

public class TimeLineActivity extends AppCompatActivity {
    private Parcelable obj;
    private String CURRENT_NAME;
    private List<Image> images;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timeline_layout);

        extractImagesFromBundle();
        initTimeLineViews();
    }

    private void extractImagesFromBundle() {
        // get images
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            boolean fromDatabase = bundle.getBoolean(ActivityUtil.DATA_TYPE);
            if (fromDatabase) {
                obj = bundle.getParcelable(ActivityUtil.ACTIVITY_MUL_IMAGE_DATA);
                images = ImageService.getImagesFromParcelable(obj);
            } else {
                obj = null;
                images = bundle.getParcelableArrayList(ActivityUtil.ACTIVITY_MUL_IMAGE_DATA);
                // recover lost dao when serialization
                ImageService.recoverDaoSession(images);
            }
            CURRENT_NAME = bundle.getString(ActivityUtil.ACTIVITY_MUL_IMAGE_TITLE_TAG);
            if (images == null) {
                images = new ArrayList<>();
            }
        } else {
            images = new ArrayList<>();
            CURRENT_NAME = "Error";
        }
    }

    private void initTimeLineViews() {
        // init views
        TimeLineImageView timeLineImageView = findViewById(R.id.timeline_test_view);
        timeLineImageView.setOffscreenPageLimit(2);
        timeLineImageView.setPageTransformer(true, new PagerViewTransform());
        final TimeLineLineView myView = findViewById(R.id.myview);

        // init adapters
        LineViewAdapter<Image> lineViewAdapter = new LineViewAdapter<Image>(this) {
            @Override
            public View getView(ViewGroup parent, int pos) {
                Image image = getItem(pos);

                View view = getLayoutInflater().inflate(R.layout.time_line_item, parent, false);

                TextView year = view.findViewById(R.id.year);
                year.setText(ImageUtils.getFormatedYear(image.getTime()));

                return view;
            }
        };

        ImageViewAdapter<Image> imageViewAdapter = new ImageViewAdapter<Image>(this) {
            @Override
            public View instantiateItem(ViewGroup container, int position) {

                Image image = getItem(position);
                View outer = getLayoutInflater().inflate(R.layout.timeline_inner, container, false);
                ImageView imageView = outer.findViewById(R.id.timeline_image);
                TextView time = outer.findViewById(R.id.timeline_time);

                // init time
                time.setText(ImageUtils.getFormatedMonthAndDay(image.getTime()));

                // init image
                ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
                layoutParams.height = layoutParams.width = 450;
                imageView.setLayoutParams(layoutParams);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                Glide.with(getApplicationContext())
                        .load(image.getPath())
                        .into(imageView);

                container.addView(outer);
                return outer;
            }
        };

        // init wrapper
        LineViewWrapperLayout<Image> lineViewWrapperLayout = new LineViewWrapperLayout<>(timeLineImageView, myView);
        lineViewWrapperLayout.init(imageViewAdapter, lineViewAdapter, images);

        // init title
        final TextView title = findViewById(R.id.timeline_title);
        title.setText(CURRENT_NAME);
    }
}
