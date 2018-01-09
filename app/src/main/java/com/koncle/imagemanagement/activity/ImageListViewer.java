package com.koncle.imagemanagement.activity;

/**
 * Created by 10976 on 2018/1/8.
 */

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.koncle.imagemanagement.R;
import com.koncle.imagemanagement.adapter.ImageAdaptor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageListViewer extends AppCompatActivity implements ImageAdaptor.ModeOperator {

    private RecyclerView recyclerView;
    private List<String> data;
    private ImageAdaptor imageAdaptor;
    private Toolbar toolbar;
    private Toolbar hidedToolbar;
    private LinearLayout operatoins;
    private TextView complete;
    private ImageButton back;
    private RadioButton share;
    private RadioButton delete;
    private RadioButton move;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }

        Bundle bundle = this.getIntent().getExtras();
        data = bundle.getStringArrayList("list");

        findViews();
        initToolbar();
        initRecyclerView();
        initOperatoins();
    }

    private void initOperatoins() {
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageAdaptor.deleteSelectedImages();
                imageAdaptor.notifyDataSetChanged();
                imageAdaptor.exitSelectMode();
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> paths = imageAdaptor.getSelections();

                ArrayList<Uri> imageUris = new ArrayList<Uri>();
                for (String path : paths) {
                    imageUris.add(Uri.fromFile(new File(path)));
                }

                Intent intent = new Intent();
                /*
                // share to wechat moment
                ComponentName comp = new ComponentName("com.tencent.mm",
                            "com.tencent.mm.ui.tools.ShareToTimeLineUI");
                intent.setComponent(comp);
                */
                intent.setAction(Intent.ACTION_SEND_MULTIPLE);
                intent.setType("image/*");

                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
                startActivity(intent);
                imageAdaptor.exitSelectMode();
            }
        });

        move.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> paths = imageAdaptor.getSelections();
                //  ImageUtils.moveImages(paths);
                imageAdaptor.exitSelectMode();
            }
        });
    }

    private void findViews() {
        hidedToolbar = findViewById(R.id.hide_toolbar);
        complete = findViewById(R.id.select_complete);
        back = findViewById(R.id.select_back);

        recyclerView = findViewById(R.id.recyclerView);

        operatoins = findViewById(R.id.operations);
        share = findViewById(R.id.share);
        delete = findViewById(R.id.delete);
        move = findViewById(R.id.move);
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Show");
        setSupportActionBar(toolbar);

        complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageAdaptor.selectAll();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageAdaptor.exitSelectMode();
            }
        });
    }

    private void initRecyclerView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4);
        recyclerView.setLayoutManager(gridLayoutManager);
        imageAdaptor = new ImageAdaptor(this, gridLayoutManager, data);
        imageAdaptor.setOperater(this);
        recyclerView.setAdapter(imageAdaptor);
        recyclerView.setItemViewCacheSize(0);
    }

    private void changeToHideBar() {
        toolbar.setVisibility(View.GONE);
        hidedToolbar.setVisibility(View.VISIBLE);
        setSupportActionBar(hidedToolbar);
    }

    private void changeToNormalBar() {
        toolbar.setVisibility(View.VISIBLE);
        hidedToolbar.setVisibility(View.GONE);
        setSupportActionBar(toolbar);
    }

    public void exitSelectMode() {
        changeToNormalBar();
        operatoins.setVisibility(View.GONE);
        /*
         * The recyclerView will retain at least 4 images to
         * have a perfect perfomance which leads to my failure
         * to recover from select mode to nomal mode,
         * Thus I have to use this method to refresh data
         * so that the view can be redrew.
         */
        imageAdaptor.notifyDataSetChanged();
    }

    public void enterSelectMode() {
        changeToHideBar();
        operatoins.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        // if there is no selected mode, then close the activity
        if (!imageAdaptor.exitSelectMode()) {
            super.onBackPressed();
        }
    }

    @Override
    public void refreshData() {
        /*
         * The recyclerView will retain at least 4 images to
         * have a perfect perfomance which leads to my failure
         * to recover from select mode to nomal mode,
         * Thus I have to use this method to refresh data
         * so that the view can be redrew.
         */
        imageAdaptor.notifyDataSetChanged();
    }

    @Override
    public void showSelectedNum(int num) {
        this.hidedToolbar.setTitle("Select " + num + " Pictures");
    }

}

