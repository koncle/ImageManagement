package com.koncle.imagemanagement.activity;

/**
 * Created by 10976 on 2018/1/8.
 */

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.koncle.imagemanagement.R;
import com.koncle.imagemanagement.adapter.ImageAdaptor;

import java.util.List;

public class ImageListViewer extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<String> data;
    private ImageAdaptor imageAdaptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        Bundle bundle = this.getIntent().getExtras();
        data = bundle.getStringArrayList("list");

        initToolbar();
        initRecyclerView();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Show");
        setSupportActionBar(toolbar);
    }


    private void initRecyclerView() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(imageAdaptor = new ImageAdaptor(this, gridLayoutManager, data));
    }

    @Override
    public void onBackPressed() {
        // if there is no selected mode, then close the activity
        if (!imageAdaptor.closeSelectMode()) {
            super.onBackPressed();
        }
    }
}

