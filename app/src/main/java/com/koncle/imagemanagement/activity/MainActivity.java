package com.koncle.imagemanagement.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.koncle.imagemanagement.R;
import com.koncle.imagemanagement.adapter.ImageAdaptor;
import com.koncle.imagemanagement.dataManagement.ImageSource;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ImageAdaptor imageAdaptor;
    private List<String> data;
    private final int SCAN_OK = 1;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        initToolbar();

        initData();

        //initRecyclerView();

    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Images");
        setSupportActionBar(toolbar);
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SCAN_OK:
                    progressDialog.dismiss();
                    //initRecyclerView();
                    showImageList();
            }
        }
    };

    private void showImageList() {
        Intent intent = new Intent(MainActivity.this, ImageListViewer.class);
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("paths", (ArrayList<String>) data);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void initData() {
        progressDialog = ProgressDialog.show(this, null, "Loading...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                data = ImageSource.getSystemPhotoList(MainActivity.this);
                mHandler.sendEmptyMessage(SCAN_OK);
            }
        }).start();
    }

    private void initRecyclerView() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4);
        recyclerView.setLayoutManager(gridLayoutManager);
        //recyclerView.setAdapter(imageAdaptor = new ImageAdaptor(this, gridLayoutManager, data));
    }
}
