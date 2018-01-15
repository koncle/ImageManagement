package com.koncle.imagemanagement.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.koncle.imagemanagement.R;
import com.koncle.imagemanagement.bean.Image;
import com.koncle.imagemanagement.dataManagement.ImageService;
import com.koncle.imagemanagement.dataManagement.ImageSource;
import com.koncle.imagemanagement.fragment.EventFragment;
import com.koncle.imagemanagement.fragment.FolderFragment;
import com.koncle.imagemanagement.fragment.HasName;
import com.koncle.imagemanagement.fragment.MapFragment;
import com.koncle.imagemanagement.fragment.Operater;
import com.koncle.imagemanagement.service.ImageListenerService;
import com.koncle.imagemanagement.util.ActivityUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Operater {

    public static final String WATCH_TAG = "folders";
    private static final int SCAN_OK_SHOW = 2;
    private static final String TAG = MainActivity.class.getSimpleName();
    private final int SCAN_OK = 1;
    private ProgressDialog progressDialog;
    private ViewPager viewPager;
    private TabLayout tab;
    private List<Fragment> fragments;
    private long start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initDataBase(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.map) {
            List<Image> images = ImageService.getAllImages();
            ActivityUtil.showMap(this, images);
        }

        return super.onOptionsItemSelected(item);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ImageService.close();

        Intent intent = new Intent(this, ImageListenerService.class);
        stopService(intent);
    }

    private void initFragments() {
        fragments = new ArrayList<>();
        fragments.add(FolderFragment.newInstance("Main", this));
        fragments.add(EventFragment.newInstance("Events", this));
        fragments.add(MapFragment.newInstance("Map", this));
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

        tab.setupWithViewPager(viewPager);
        tab.setTabMode(TabLayout.MODE_FIXED);
    }

    private void findViews() {
        viewPager = findViewById(R.id.main_viewpager);
        tab = findViewById(R.id.tab);
    }

    private void initDataBase(boolean refresh) {
        ImageService.init(this);

        SharedPreferences sp = this.getSharedPreferences("dbPre", MODE_PRIVATE);
        if (refresh || !sp.getBoolean("create", false)) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("create", true);
            editor.apply();
            ImageService.refreshTables();
            initData(true);
        } else {
            initData(false);
        }
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SCAN_OK_SHOW:
                    progressDialog.dismiss();
                    show();
                    break;
                case SCAN_OK:
                    List<Image> folders = ImageService.getFolders();
                    ((FolderFragment) fragments.get(0)).setFolders(folders);
                    break;
            }
        }
    };

    private void initData(boolean load) {
        if (!load) {
            show();
        } else {
            progressDialog = ProgressDialog.show(this, null, "Loading...");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ImageSource.getSystemPhotoList(MainActivity.this);
                    mHandler.sendEmptyMessage(SCAN_OK_SHOW);
                }
            }).start();
        }
    }

    private void show() {
        findViews();
        initToolbar();
        initFragments();
        initViewPager();
        initWatcher();
    }

    public void refreshFolders() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ImageSource.getSystemPhotoList(MainActivity.this);
                mHandler.sendEmptyMessage(SCAN_OK);
            }
        }).start();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // title.setText("ImageManagement");
        setSupportActionBar(toolbar);
    }

    private static final int FOLDER_FRAGMENT = 0;
    private static final int EVENT_FRAGMENT = 1;
    private static final int MAP_FRAGMENT = 2;

    @Override
    public void onBackPressed() {
        int pos = viewPager.getCurrentItem();
        Fragment fragment = fragments.get(pos);
        switch (pos) {
            case FOLDER_FRAGMENT:
                if (!((FolderFragment) fragment).exitSelectMode())
                    super.onBackPressed();
                break;
            case EVENT_FRAGMENT:
                super.onBackPressed();
                break;
            case MAP_FRAGMENT:
                super.onBackPressed();
                break;
            default:
                super.onBackPressed();
                break;
        }
    }
}
