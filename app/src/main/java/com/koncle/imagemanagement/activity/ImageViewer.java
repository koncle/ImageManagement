package com.koncle.imagemanagement.activity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;

import com.koncle.imagemanagement.R;
import com.koncle.imagemanagement.adapter.ImageViewPagerAdapter;
import com.koncle.imagemanagement.util.ImageUtils;

import java.io.File;
import java.util.List;

/**
 * Created by 10976 on 2018/1/10.
 */

public class ImageViewer extends AppCompatActivity implements ImageViewPagerAdapter.ModeChange {
    private ViewPager imageViewPager;
    private List<String> paths;
    private View delete;
    private View share;
    private View mark;
    private View move;
    private Toolbar toolbar;
    private LinearLayout toolLayout;
    private ImageViewPagerAdapter pagerAdapter;

    private boolean toolMode = true;

    public static final String RESULT_TAG = "result";
    public static final int IMAGE_VIEWER_DELETE = 2;
    public static final int IMAGE_VIWER_SCROLL = 3;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_view_layout);

        Bundle bundle = getIntent().getExtras();
        int position = (int) bundle.get("pos");
        this.paths = (List<String>) bundle.get("paths");

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
        delete = findViewById(R.id.delete);
        share = findViewById(R.id.share);
        move = findViewById(R.id.move);
        mark = findViewById(R.id.mark);
        toolLayout = findViewById(R.id.tool_layout);
    }

    private void initViewPager(int position) {
        pagerAdapter = new ImageViewPagerAdapter(this, paths);
        pagerAdapter.setOperater(this);
        imageViewPager.setAdapter(pagerAdapter);
        imageViewPager.setCurrentItem(position);
    }

    private void initOperatoins() {
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean result = ImageUtils.deleteFile(paths.get(imageViewPager.getCurrentItem()));

                Intent intent = new Intent();
                intent.putExtra(RESULT_TAG, result);
                setResult(IMAGE_VIEWER_DELETE, intent);
                finish();
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.fromFile(new File(paths.get(imageViewPager.getCurrentItem())));
                Intent intent = new Intent();

                intent.setAction(Intent.ACTION_SEND);
                intent.setType("image/*");

                intent.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(intent);
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
                startActivityForResult(new Intent(ImageViewer.this, RemarkActivity.class), 0);
            }
        });
    }

    public void toggleMode() {
        if (toolMode == true) {
            hideTools();
            toolMode = false;
        } else {
            showTools();
            toolMode = true;
        }
    }

    public void showTools() {
        toolLayout.setVisibility(View.VISIBLE);
        toolbar.setVisibility(View.VISIBLE);
        imageViewPager.setBackgroundColor(Color.BLACK);
    }

    public void hideTools() {
        toolLayout.setVisibility(View.GONE);
        toolbar.setVisibility(View.GONE);
        imageViewPager.setBackgroundColor(Color.WHITE);
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("pos", imageViewPager.getCurrentItem());
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
