package com.koncle.imagemanagement.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.koncle.imagemanagement.R;
import com.koncle.imagemanagement.adapter.ImageAdaptor;
import com.koncle.imagemanagement.bean.Image;
import com.koncle.imagemanagement.dataManagement.ImageService;
import com.koncle.imagemanagement.dataManagement.ImageSource;
import com.koncle.imagemanagement.fragment.FolderFragment;
import com.koncle.imagemanagement.fragment.HasName;
import com.koncle.imagemanagement.service.ImageListenerService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String WATCH_TAG = "folders";
    private RecyclerView recyclerView;
    private ImageAdaptor imageAdaptor;
    private final int SCAN_OK = 1;
    private ProgressDialog progressDialog;
    private ViewPager viewPager;
    private TabLayout tab;
    private List<Fragment> fragments;
    private boolean start = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initDataBase();

        initData(true);

        findViews();

        //ImageService.getAllImages();

        //initRecyclerView();

    }

    private void initWatcher() {
        List<String> folders = new ArrayList<>();
        for (Image i : ImageService.getFolders()) {
            int index = i.getPath().lastIndexOf("/");
            folders.add(i.getPath().substring(0, index));
        }
        Intent intent = new Intent(this, ImageListenerService.class);
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(WATCH_TAG, (ArrayList<String>) folders);
        intent.putExtras(bundle);
        startService(intent);
    }

    private void destroyWatcherService() {
        Intent intent = new Intent(this, ImageListenerService.class);
        stopService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ImageService.close();

        destroyWatcherService();
    }

    private void initFragments() {
        fragments = new ArrayList<>();
        fragments.add(FolderFragment.newInstance("Main"));
        fragments.add(FolderFragment.newInstance("Events"));
        fragments.add(FolderFragment.newInstance("Map"));
    }

    private void initViewPager() {
        viewPager.setAdapter(new FragmentPagerAdapter(this.getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return fragments.get(position);
            }

            @Override
            public int getCount() {
                return fragments == null ? 0 : fragments.size();
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return ((HasName) fragments.get(position)).getName();
            }
        });
        //viewPager.setPageTransformer(false, new MAnimation());
        viewPager.setOffscreenPageLimit(2); // 最大缓存页数 为 limit * 2 + 1
    }

    private void findViews() {
        viewPager = findViewById(R.id.main_viewpager);
        tab = findViewById(R.id.tab);
    }

    private void initTab() {
        tab.setupWithViewPager(viewPager);
        tab.setTabMode(TabLayout.MODE_FIXED);
    }

    private void initDataBase() {
        ImageService.init(this);
        ImageService.refreshTables();
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SCAN_OK:
                    progressDialog.dismiss();
                    //initRecyclerView();

                    initFragments();
                    initViewPager();
                    initTab();

                    //initWatcher();

                    //ActivityUtil.showImageList(MainActivity.this, data);
            }
        }
    };

    private void initData(boolean load) {
        if (!load) {
            initFragments();
            initViewPager();
            initTab();
            return;
        } else {
            progressDialog = ProgressDialog.show(this, null, "Loading...");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ImageSource.getSystemPhotoList(MainActivity.this);
                    mHandler.sendEmptyMessage(SCAN_OK);
                }
            }).start();
        }
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Images");
        setSupportActionBar(toolbar);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ImageListenerService.class);
                if (!start) {
                    startService(intent);
                } else {
                    stopService(intent);
                }
            }
        });
    }

    private void initRecyclerView() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4);
        recyclerView.setLayoutManager(gridLayoutManager);
        //recyclerView.setAdapter(imageAdaptor = new ImageAdaptor(this, gridLayoutManager, data));
    }
}
