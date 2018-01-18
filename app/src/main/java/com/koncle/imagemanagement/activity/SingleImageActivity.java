package com.koncle.imagemanagement.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import com.koncle.imagemanagement.R;
import com.koncle.imagemanagement.adapter.SingleImageViewPagerAdapter;
import com.koncle.imagemanagement.bean.Image;
import com.koncle.imagemanagement.dataManagement.ImageService;
import com.koncle.imagemanagement.fragment.SingleImageDIalogFragment;
import com.koncle.imagemanagement.util.ActivityUtil;
import com.koncle.imagemanagement.util.ImageUtils;

import java.util.ArrayList;
import java.util.List;

import static android.view.Window.FEATURE_CONTENT_TRANSITIONS;

/**
 * Created by 10976 on 2018/1/10.
 */

public class SingleImageActivity extends AppCompatActivity implements SingleImageViewPagerAdapter.ModeChange {
    public static final String DELETE_IMAGE = "image";
    private ViewPager imageViewPager;
    private List<Image> images;
    private View delete;
    private View share;
    private View mark;
    private View move;
    private Toolbar toolbar;
    private LinearLayout toolLayout;
    private SingleImageViewPagerAdapter pagerAdapter;

    private boolean toolMode = true;

    public static final String RESULT_TAG = "result";
    public static final int IMAGE_VIEWER_DELETE = 2;
    public static final int IMAGE_VIWER_SCROLL = 3;
    private List<Image> deleteImages;
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    private RadioButton tag;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(FEATURE_CONTENT_TRANSITIONS);

        setContentView(R.layout.single_view_layout);

        Bundle bundle = getIntent().getExtras();
        int position = (int) bundle.get("pos");
        this.images = (List<Image>) bundle.get("images");
        ImageService.recoverDaoSession(images);  // its daoSession can't not be parsed

        deleteImages = new ArrayList<>();

        findViews();

        initViewPager(position);
        initToolbar();
        initOperatoins();
        //hideStatusBarAndActionBar();
    }

    private void initToolbar() {
        this.setSupportActionBar(toolbar);
    }

    private void hideStatusBarAndActionBar() {
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }

    private void findViews() {
        toolbar = findViewById(R.id.single_toolbar);
        imageViewPager = findViewById(R.id.image_view_pager);
        delete = findViewById(R.id.event_delete);
        share = findViewById(R.id.share);
        move = findViewById(R.id.move);
        mark = findViewById(R.id.mark);
        tag = findViewById(R.id.single_tag);
        toolLayout = findViewById(R.id.tool_layout);

        bottomSheetBehavior = BottomSheetBehavior.from(toolLayout);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        hideTools();
    }

    private void initViewPager(int position) {
        pagerAdapter = new SingleImageViewPagerAdapter(this, images, position);
        // interface for adapter
        pagerAdapter.setOperator(this);
        imageViewPager.setAdapter(pagerAdapter);
        imageViewPager.setCurrentItem(position);

        imageViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                changeTitle(images.get(position).getName());
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void initOperatoins() {
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Image currentImage = images.get(imageViewPager.getCurrentItem());
                deleteImage(currentImage);
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.shareImage(SingleImageActivity.this, images.get(imageViewPager.getCurrentItem()));
            }
        });

        move.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        mark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(SingleImageActivity.this, RemarkActivity.class), 0);
            }
        });


        tag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SingleImageDIalogFragment dialog = SingleImageDIalogFragment.newInstance(images.get(imageViewPager.getCurrentItem()));
                dialog.show(getSupportFragmentManager(), "Single");
            }
        });
    }

    public void deleteImage(Image currentImage) {
        // delete from sd card
        boolean result = ImageUtils.deleteFile(currentImage.getPath());

        // delete from database
        // replaced by service
        // ImageService.deleteImage(currentImage);

        // don't need to delete from memory, cause this activity will be destroyed
        Intent intent = new Intent();
        intent.putExtra(RESULT_TAG, result);
        if (result) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(DELETE_IMAGE, currentImage);
            intent.putExtras(bundle);
        }
        setResult(IMAGE_VIEWER_DELETE, intent);
        finish();
    }

    @Override
    public void addDeleteImage(Image image) {
        this.deleteImages.add(image);
    }

    public void toggleMode() {
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            hideTools();
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        } else {
            showTools();
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    @Override
    public void changeTitle(String s) {
        this.toolbar.setTitle(s);
    }

    public void showTools() {
        //toolLayout.setVisibility(View.VISIBLE);
        toolbar.setVisibility(View.VISIBLE);
        imageViewPager.setBackgroundColor(Color.BLACK);
    }

    public void hideTools() {
        //toolLayout.setVisibility(View.GONE);
        toolbar.setVisibility(View.GONE);
        imageViewPager.setBackgroundColor(Color.WHITE);
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("pos", imageViewPager.getCurrentItem());

        if (deleteImages.size() > 0) {
            intent.putExtra("delete", true);
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("deletes", (ArrayList<? extends Parcelable>) deleteImages);
            intent.putExtras(bundle);
        } else {
            intent.putExtra("delete", false);
        }
        setResult(IMAGE_VIWER_SCROLL, intent);
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RemarkActivity.REMARK_OK) {
            String remark = data.getStringExtra(RemarkActivity.REMARK_INPUT);
        }
    }
}
